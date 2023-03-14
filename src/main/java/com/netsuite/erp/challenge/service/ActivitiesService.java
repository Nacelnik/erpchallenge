package com.netsuite.erp.challenge.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsuite.erp.challenge.dataobject.Activity;
import com.netsuite.erp.challenge.dataobject.Athlete;
import com.netsuite.erp.challenge.repository.ActivityRepository;
import com.netsuite.erp.challenge.repository.AthleteRepository;
import com.netsuite.erp.challenge.utilities.JsonBodyHandler;
import com.netsuite.erp.challenge.utilities.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ActivitiesService
{
    public static final int TOO_MANY_REQUESTS = 429;
    @Autowired
    AthleteRepository athleteRepository;

    @Autowired
    ActivityRepository activityRepository;

    private static final String ACTIVITIES = "https://www.strava.com/api/v3/activities";

    @RequestMapping("/refresh/{month}")
    public String refreshActivities(@PathVariable() Integer month) throws IOException, InterruptedException
    {
        // 1 all athletes
        // for each: check if token is ok, if not, get refresh token
        // get activities
        // diff against DB
        // get and save new activities
        List<Athlete> athletes = athleteRepository.findAll();

        StringBuilder resultBuilder = new StringBuilder();

        for (Athlete athlete : athletes)
        {
            if (!athlete.active)
                continue;

            var client = HttpClient.newHttpClient();

            if (athlete.tokenExpiration() == null || athlete.tokenExpiration().isBefore(LocalDateTime.now()))
            {
                System.out.println(athlete.tokenExpiration());

                var tokenRequest = HttpRequest.newBuilder(
                        URI.create(AuthorizationService.STRAVA_TOKEN))
                        .POST(HttpRequest.BodyPublishers.ofString(
                            UrlUtil.getFormDataAsString(Map.of(
                                        "client_id",   AuthorizationService.CLIENT_ID,
                                        "client_secret", "ba680a1314ceb669fdfacacc246d732e7c49598a",
                                        "refresh_token", Objects.requireNonNull(athlete.refreshToken),
                                        "grant_type", "refresh_token"
                                ))
                        ))
                        .header("Accept", "application/json")
                        .build();

                var response = client.send(tokenRequest, new JsonBodyHandler<>(TokenRefresh.class));
                System.out.println(response.body());

                System.out.println(response.body().access_token);
                System.out.println(response.body().refresh_token);
                System.out.println(response.body().expires_at);

                athlete.accessExpiration = Long.valueOf(response.body().expires_at);
                athlete.accessToken = response.body().access_token;
                athlete.refreshToken = response.body().refresh_token;
                athleteRepository.save(athlete);
            }

            LocalDate monthDate = LocalDate.of(2023, month, 1);
            if (monthDate.isAfter(LocalDate.now()))
                return "There can be no future activities";

            String requestUrl = ACTIVITIES + "?access_token=" + athlete.accessToken
                    + "&per_page=200"
                    + "&page=%d&after="
                    + monthDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                    + "&before="
                    + (monthDate.plusMonths(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) - 1);

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (int i = 1; i < 6; i++) {
                var activitiesRequest = HttpRequest.newBuilder(
                                URI.create(String.format(requestUrl, i))
                        )
                        .build();

                var response = client.send(activitiesRequest, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == TOO_MANY_REQUESTS)
                    return "Requests limit reached, exiting.<br/>" + resultBuilder;

                ActivitiesResponse[] activitiesResponses = objectMapper.readValue(response.body(), ActivitiesResponse[].class);

                for (ActivitiesResponse activityResponse : activitiesResponses) {
                    Activity activity = activityRepository.findByStravaId(activityResponse.id);
                    if (activity == null) {
                        getAndSaveActivity(client, athlete, activityResponse.id);
                        resultBuilder.append("Found new activity ")
                                .append(activityResponse.id)
                                .append(" for athlete ")
                                .append(athlete.firstName)
                                .append(" ").
                                append(athlete.lastName)
                                .append("<br/>");
                    }
                }

                // if we didn't get 200 activities, don't request anything
                if (activitiesResponses.length < 200)
                {
                    System.out.println("No more activities, exiting loop at " + i);
                    break;
                }
            }
        }

        resultBuilder.append("Finished successfully<br/>");
        return resultBuilder.toString();
    }

    @RequestMapping("/activities")
    public String listActivities()
    {
        StringBuilder result = new StringBuilder();

        result.append("<table>");

        List<Athlete> athletes = athleteRepository.findAll();
        for (Athlete athlete : athletes)
        {
            result.append("<tr>");
            result.append("<td>");
            result.append(athlete.firstName).append(" ").append(athlete.lastName);
            result.append("</td><td>");
            result.append(athlete.tribe);
            result.append("</td><td>");
            List<Activity> activities = activityRepository.findByAthleteId(athlete.id);
            result.append(activities.size());
            result.append("</td><td>");
            result.append(activities.stream().map(a -> a.calories).reduce(BigDecimal.ZERO, BigDecimal::add));
            result.append("</td></tr>");
        }

        return result.toString();
    }

    private void getAndSaveActivity(HttpClient client, Athlete athlete, String id) throws IOException, InterruptedException {
        var activitiesRequest = HttpRequest.newBuilder(
                        URI.create(ACTIVITIES + "/" + id + "?access_token=" + athlete.accessToken)
                )
                .build();

        var response = client.send(activitiesRequest, new JsonBodyHandler<>(ActivityResponse.class));

        BigDecimal calories = response.body().calories != null ? new BigDecimal(response.body().calories) : BigDecimal.ZERO;
        var activity = new Activity(response.body().id, athlete.id, calories, stringToDate(response.body().start_date_local));
        activityRepository.save(activity);
    }

    private static String stringToDate(String dateAsString) {
        if (dateAsString != null)
        {
            LocalDate date = LocalDate.parse(dateAsString.split("T")[0]);
            return date.format(DateTimeFormatter.ISO_DATE);
        }
        return null;
    }

    private static class ActivitiesResponse
    {
        public String id;
    }

    private static class ActivityResponse
    {
        public String id;
        public String calories;
        public String start_date_local;
    }

    private static class TokenRefresh
    {
        public String access_token;
        public String expires_at;
        public String refresh_token;
    }
}
