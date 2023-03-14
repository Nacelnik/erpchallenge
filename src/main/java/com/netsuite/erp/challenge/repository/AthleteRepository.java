package com.netsuite.erp.challenge.repository;

import com.netsuite.erp.challenge.dataobject.Athlete;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AthleteRepository extends MongoRepository<Athlete, String>
{
    Athlete findByStravaId(String stravaId);
}
