package com.netsuite.erp.challenge.service;

import com.netsuite.erp.challenge.dataobject.Athlete;
import com.netsuite.erp.challenge.repository.AthleteRepository;
import com.netsuite.erp.challenge.utilities.Tribes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AthleteService
{
    @Autowired
    AthleteRepository athleteRepository;

    @RequestMapping("/athletes")
    public String listAthletes()
    {
        List<Athlete> athletes = athleteRepository.findAll();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table><tr>");
        athletes.forEach(athlete -> stringBuilder.append(athlete.toList()));
        stringBuilder.append("</tr></table>");
        return stringBuilder.toString();
    }

    @RequestMapping("/athlete/{id}")
    public String athlete(@PathVariable(name = "id") String stravaId)
    {
        Athlete athlete = athleteRepository.findByStravaId(stravaId);
        return athlete.name() + "<br/> Assign to a tribe <br/>" + createTribeLinks(athlete.stravaId)
                + createInActivateLink(athlete);
    }

    @RequestMapping("/athlete/inactivate/{id}")
    public String inactivate(@PathVariable(name = "id") String stravaId)
    {
        Athlete athlete = athleteRepository.findByStravaId(stravaId);
        athlete.active = false;
        athleteRepository.save(athlete);
        return "Successfully inactivated " + athlete.name();
    }

    @RequestMapping("/athlete/activate/{id}")
    public String activate(@PathVariable(name = "id") String stravaId)
    {
        Athlete athlete = athleteRepository.findByStravaId(stravaId);
        athlete.active = true;
        athleteRepository.save(athlete);
        return "Successfully activated " + athlete.name();
    }

    @RequestMapping("/athlete/{id}/{tribe}")
    public String setTribe(@PathVariable(name = "id") String stravaId, @PathVariable(name = "tribe") String tribe)
    {
        Athlete athlete = athleteRepository.findByStravaId(stravaId);
        athlete.tribe = tribe;
        athleteRepository.save(athlete);
        return "Assigned " + athlete.name() + " to " + tribe;
    }

    private String createTribeLinks(String stravaId)
    {
        return
                "<table><tr>" +
                        EnumSet.allOf(Tribes.class).stream().map(tribe -> "<td><a href='/athlete/" + stravaId + "/" + tribe + "'> " + tribe + "</a></td>").collect(Collectors.joining(""))
                +"</tr></table>";
    }

    private String createInActivateLink(Athlete athlete)
    {
        return
                athlete.active ?
                        "<br/> <a href='/athlete/inactivate/" + athlete.stravaId + "'>Inactivate</a>" :
                        "<br/> <a href='/athlete/activate/" + athlete.stravaId + "'>Activate</a>";
    }
}
