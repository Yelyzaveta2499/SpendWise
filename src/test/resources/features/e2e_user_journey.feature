Feature: E2E User Journey

  Background:
    * url baseUrl

  Scenario: User logs in, adds an expense, views dashboard, and logs out
    # Login
    Given path 'login'
    And form field 'username' = 'indiv'
    And form field 'password' = 'password123'
    When method post
    Then status 200
    # Add expense (simulate session/cookie if needed)
    Given path 'api/expenses'
    And request { name: 'Groceries', category: 'Food & Dining', amount: 25.50, date: '2026-03-17' }
    When method post
    Then status 201
    # View dashboard
    Given path 'dashboard'
    When method get
    Then status 200
    # Logout
    Given path 'logout'
    When method post
    Then status 200

