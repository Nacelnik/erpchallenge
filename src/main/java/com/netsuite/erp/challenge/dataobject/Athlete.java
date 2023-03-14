package com.netsuite.erp.challenge.dataobject;

import com.mongodb.lang.Nullable;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Athlete
{
    @Id
    public String id;

    @Nullable
    public String firstName;

    @Nullable
    public String lastName;

    @Nullable
    public String stravaId;

    @Nullable
    public String tribe;

    @Nullable
    public String accessToken;

    @Nullable
    public String refreshToken;

    @Nullable
    public Long accessExpiration;

    public Boolean active;

    public Athlete(String stravaId, String firstName, String lastName)
    {
        this.stravaId = stravaId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = true;
    }

    public LocalDateTime tokenExpiration()
    {
        if (accessExpiration != null)
            return LocalDateTime.ofEpochSecond(accessExpiration, 0, ZoneOffset.UTC);
        return
               null;
    }

    @Override
    public String toString() {
        return "Athlete{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", stravaId='" + stravaId + '\'' +
                ", tribe='" + tribe + '\'' +
                ", expiration='" + tokenExpiration() + "'" +
                '}';
    }

    public String toList()
    {
        return "<tr><td><a href='athlete/" + stravaId + "'>" + firstName + " " + lastName + "</td><td>" + tribe + "</tr></tr>";
    }

    public String name() {
        return firstName + " " + lastName;
    }
}
