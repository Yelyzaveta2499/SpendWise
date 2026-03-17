package com.example.SpendWise.karate;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Disabled;


@Disabled("Karate disabled on JDK 23 due to GraalJS incompatibility")
public class RestApiKarateIT {

    @Karate.Test
    Karate runAll() {
        // Run all feature files including new detailed Karate tests
        return Karate.run(
            "classpath:features/e2e_user_journey.feature",
            "classpath:features/api_expense_crud.feature",
            "classpath:features/karate/expenses.feature",
            "classpath:features/karate/users.feature"
        );
    }
}
