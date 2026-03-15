Feature: E2E login and logout flow

  Background:
    * url baseUrl

  # Happy path login for an individual user. Adjust credentials to match test data.
  Scenario: login with valid credentials succeeds
    Given path 'login'
    And form field 'username' = 'indiv'
    And form field 'password' = 'password'
    When method post
    Then status 200

  # Invalid credentials should not authenticate.
  Scenario: login with invalid password fails
    Given path 'login'
    And form field 'username' = 'indiv'
    And form field 'password' = 'wrong-password'
    When method post
    Then status 4xx

