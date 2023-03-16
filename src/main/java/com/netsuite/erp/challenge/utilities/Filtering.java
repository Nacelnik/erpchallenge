package com.netsuite.erp.challenge.utilities;

import com.netsuite.erp.challenge.dataobject.Activity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Filtering
{
    public static List<Activity> filterByMonth(List<Activity> activities, Integer month)
    {
        return activities.stream()
                .filter(d -> d.startDate != null && LocalDate.parse(d.startDate, DateTimeFormatter.ISO_DATE).getMonth().getValue() == month).toList();
    }

}
