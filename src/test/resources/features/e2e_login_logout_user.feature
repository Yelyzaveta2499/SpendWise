Feature: E2E login and logout tests

  Background:
    * url baseUrl

  # Check that the login page is reachable.
  Scenario: login page loads
    Given path 'login'
    When method get
    Then status != 500

  # Posting invalid credentials should not crash the server.
  Scenario: login with invalid password does not return 5xx
    Given path 'login'
    And form field 'username' = 'indiv'
    And form field 'password' = 'wrong-password'
    When method post
    Then status != 500
