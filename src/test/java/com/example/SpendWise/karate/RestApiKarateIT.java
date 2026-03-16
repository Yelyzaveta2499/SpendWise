package com.example.SpendWise.karate;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Disabled;


@Disabled("Karate disabled on JDK 23 due to GraalJS incompatibility")
public class RestApiKarateIT {

    @Karate.Test
    Karate runAll() {
        // Would look for src/test/resources/features/**/*
        return Karate.run("classpath:features");
    }
}
