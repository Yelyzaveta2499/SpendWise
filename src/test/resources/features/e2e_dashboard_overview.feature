Feature: E2E dashboard overview

  Background:
    * url baseUrl

  # Basic smoke test to ensure the dashboard page is reachable for an authenticated user.
  # Depending on security setup - > may need to first call the login feature
  # and reuse cookies / token.
  Scenario: dashboard page loads
    Given path 'dashboard'
    When method get
    Then status 200

