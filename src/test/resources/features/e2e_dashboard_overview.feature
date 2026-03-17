Feature: E2E dashboard overview

  Background:
    * url baseUrl

  # smoke test: dashboard endpoint responds without 5xx.
  Scenario: dashboard endpoint responds
    Given path 'dashboard'
    When method get
    Then status < 500
