package com.example.SpendWise.karate;

import com.intuit.karate.junit5.Karate;

public class RestApiKarateIT {

    @Karate.Test
    Karate runAll() {

        return Karate.run("classpath:features");
    }
}

