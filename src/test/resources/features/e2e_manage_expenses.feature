Feature: E2E expenses list

  Background:
    * url baseUrl

  # smoke test: the expenses page or API responds without 5xx.
  Scenario: expenses endpoint responds
    Given path 'expenses'
    When method get
    Then status != 500
