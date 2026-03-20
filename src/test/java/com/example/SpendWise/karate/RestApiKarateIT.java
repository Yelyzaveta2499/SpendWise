package com.example.SpendWise.karate;

import com.intuit.karate.core.MockServer;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

class RestApiKarateIT {

    private static MockServer mock;

    @BeforeAll
    static void startMockServer() {
        mock = MockServer.feature("classpath:karate/mock-api.feature").http(1111).build();
        System.setProperty("karate.baseUrl", "http://localhost:1111");
    }

    @AfterAll
    static void stopMockServer() {
        if (mock != null) {
            mock.stop();
        }
        System.clearProperty("karate.baseUrl");
    }

    @Karate.Test
    Karate runAll() {
        return Karate.run("classpath:karate").tags("~@mock");
    }

}