package com.netsuite.erp.challenge.service;

import com.netsuite.erp.challenge.dataobject.Athlete;
import com.netsuite.erp.challenge.repository.AthleteRepository;
import com.netsuite.erp.challenge.utilities.JsonBodyHandler;
import com.netsuite.erp.challenge.utilities.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthorizationService {
    public static final String CLIENT_ID = "103419";
    private static final String STRAVA_AUTHORIZATION = "http://www.strava.com/oauth/authorize?client_id=" + CLIENT_ID + "&response_type=code&redirect_uri=http://localhost:8080/exchange_token&approval_prompt=force&scope=profile:read_all,activity:read_all";
    public static final String STRAVA_TOKEN = "https://www.strava.com/api/v3/oauth/token";

    @Autowired
    AthleteRepository athleteRepository;


    //TODO: We probably need to save the code and ask for the tokens everytime
    @RequestMapping("/exchange_token")
    public String authorizeUser(@RequestParam String code) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();

        var tokensRequest = HttpRequest.newBuilder(
                        URI.create(STRAVA_TOKEN))
                .POST(HttpRequest.BodyPublishers.ofString(
                        UrlUtil.getFormDataAsString(Map.of(
                                "client_id",   CLIENT_ID,
                                "client_secret", "ba680a1314ceb669fdfacacc246d732e7c49598a",
                                "code", code,
                                "grant_type", "authorization_code"
                        ))
                ))
                .header("Accept", "application/json")
                .build();

        var response = client.send(tokensRequest, new JsonBodyHandler<>(AuthorizationResponse.class));

        Athlete athlete = response.body().toAthlete();
        Athlete existingAthlete = athleteRepository.findByStravaId(athlete.stravaId);

        if (existingAthlete != null)
            athlete.id = existingAthlete.id;

        athleteRepository.save(athlete);
        return "Authorized successfully " + athlete.name();
    }

    @RequestMapping("/adduser")
    public String addUser() {
        return "<a href='" + STRAVA_AUTHORIZATION + "'>Authorize</a>";
    }

    private static class AuthorizationResponse
    {
        public String access_token;
        public String refresh_token;
        public String expires_at;
        public AthleteInfo athlete;

        public Athlete toAthlete()
        {
            Athlete athlete = new Athlete(this.athlete.id, this.athlete.firstname, this.athlete.lastname);
            athlete.accessToken = access_token;
            athlete.refreshToken = refresh_token;
            athlete.accessExpiration = Long.parseLong(expires_at);
            return athlete;
        }
    }

    private static class AthleteInfo
    {
        public String id;
        public String firstname;
        public String lastname;
    }
}
