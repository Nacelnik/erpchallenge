package com.netsuite.erp.challenge.dataobject;

import com.mongodb.lang.Nullable;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

public class Activity
{
    @Id
    public String id;

    public String stravaId;

    public String athleteId;

    public BigDecimal calories;

    @Nullable
    public String startDate;

    public Activity(String stravaId, String athleteId, BigDecimal calories, String startDate)
    {
        this.stravaId = stravaId;
        this.athleteId = athleteId;
        this.calories = calories;
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id='" + id + '\'' +
                ", stravaId='" + stravaId + '\'' +
                ", athleteId='" + athleteId + '\'' +
                ", calories=" + calories +
                '}';
    }
}
