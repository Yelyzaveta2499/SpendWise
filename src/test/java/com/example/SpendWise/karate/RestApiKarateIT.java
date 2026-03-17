package com.example.SpendWise.karate;

import com.intuit.karate.junit5.Karate;


public class RestApiKarateIT {

    @Karate.Test
    Karate runAll() {
        // Recursively run all feature files in both 'features' and 'karate' folders
        return Karate.run("classpath:features", "classpath:karate");
    }
}
