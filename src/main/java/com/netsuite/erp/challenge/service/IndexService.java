package com.netsuite.erp.challenge.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexService
{

    @RequestMapping("/")
    public String home()
    {
        return """
                <a href="/adduser">Authorize new user</a><br/>
                <a href="/athletes">List athletes</a><br/>
                <a href="/activities">List calories per athlete</a><br/>
                <a href="/leaderboard">Overall leaderboard</a><br/>
                """
                ;
    }
}
