package com.example.SpendWise.karate;

import com.intuit.karate.junit5.Karate;


public class RestApiKarateIT {

    @Karate.Test
    Karate runAll() {
        // Run all feature files including new detailed Karate tests
        return Karate.run(
            "classpath:features/e2e_user_journey.feature",
            "classpath:features/e2e_manage_expenses.feature",
            "classpath:features/e2e_login_logout_user.feature",
            "classpath:features/e2e_dashboard_overview.feature",
            "classpath:features/api_expense_crud.feature",
            "classpath:karate/expenses.feature",
            "classpath:karate/users.feature",
            "classpath:karate/budgets.feature",
            "classpath:karate/dashboard.feature",
            "classpath:karate/goals.feature",
            "classpath:karate/tags.feature",
            "classpath:karate/reports.feature",
            "classpath:karate/ai.feature",
            "classpath:karate/expenses-search.feature",
            "classpath:karate/settings.feature"
        );
    }
}
