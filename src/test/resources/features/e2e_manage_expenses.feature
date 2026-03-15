Feature: E2E expenses list

  Background:
    * url baseUrl

  # smoke test: the expenses page or API should be reachable.
  # Adjust the path to match controller mapping.
  Scenario: expenses page loads
    Given path 'expenses'
    When method get
    Then status 200

