package com.netsuite.erp.challenge.repository;

import com.netsuite.erp.challenge.dataobject.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String>
{
    Activity findByStravaId(String stravaId);

    List<Activity> findByAthleteId(String athleteId);
}
