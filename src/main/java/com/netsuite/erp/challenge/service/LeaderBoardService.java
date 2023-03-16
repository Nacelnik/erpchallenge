package com.netsuite.erp.challenge.service;

import com.netsuite.erp.challenge.dataobject.Activity;
import com.netsuite.erp.challenge.dataobject.Athlete;
import com.netsuite.erp.challenge.repository.ActivityRepository;
import com.netsuite.erp.challenge.repository.AthleteRepository;
import com.netsuite.erp.challenge.utilities.Filtering;
import com.netsuite.erp.challenge.utilities.Sorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LeaderBoardService
{
    @Autowired
    AthleteRepository athleteRepository;

    @Autowired
    ActivityRepository activityRepository;

    @RequestMapping("/leaderboard")
    public String getLeaderboard()
    {
        return createLeaderboard(null);
    }

    @RequestMapping("/leaderboard/{month}")
    public String getLeaderboard(@PathVariable String month)
    {
        return createLeaderboard(month);
    }

    private String createLeaderboard(String month)
    {
        List<Athlete> athletes = athleteRepository.findAll();

        Map<Athlete, BigDecimal> caloriesPerAthlete = getCaloriesPerAthlete(month, athletes);
        Map<String, BigDecimal> caloriesPerTribe = getCaloriesPerTribe(caloriesPerAthlete);
        List<Map.Entry<String, BigDecimal>> leaderboard = Sorting.sortCalories(caloriesPerTribe);

        StringBuilder result = new StringBuilder();

        createTable(leaderboard, result);

        result.append("<a href='/leaderboard'></a>Overall leaderboard<br>");

        for (int i = 3; i<=12; i++)
        {
            result.append(String.format("<a href='/leaderboard/%d'>Month %d</a><br>", i, i));
        }

        return result.toString();
    }

    private Map<Athlete, BigDecimal> getCaloriesPerAthlete(String month, List<Athlete> athletes) {
        Map<Athlete, BigDecimal> caloriesPerAthlete = new HashMap<>();
        athletes.forEach(
                athlete ->
                {
                    BigDecimal totalCalories = countCalories(athlete, month != null ? Integer.parseInt(month) : null);
                    caloriesPerAthlete.put(athlete, totalCalories);
                }

        );
        return caloriesPerAthlete;
    }

    private Map<String, BigDecimal> getCaloriesPerTribe(Map<Athlete, BigDecimal> caloriesPerAthlete) {
        Map<String, BigDecimal> caloriesPerTribe = new HashMap<>();
        caloriesPerAthlete.forEach((athlete, calories) ->
                {
                    BigDecimal tribeCal = caloriesPerTribe.getOrDefault(athlete.tribe, BigDecimal.ZERO);
                    tribeCal = tribeCal.add(calories);
                    caloriesPerTribe.put(athlete.tribe, tribeCal);
                }
        );
        return caloriesPerTribe;
    }

    private void createTable(List<Map.Entry<String, BigDecimal>> leaderboard, StringBuilder result) {
        result.append("<table>");
        leaderboard.forEach(
                entry ->
                {
                    result.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
                }
        );
        result.append("</table>");
    }


    private BigDecimal countCalories(Athlete athlete, Integer month)
    {
        List<Activity> activities = activityRepository.findByAthleteId(athlete.id);
        List<Activity> filtered = activities;

        if (month != null)
        {
             filtered = Filtering.filterByMonth(activities, month);
        }

        return filtered.stream().map(a -> a.calories).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
