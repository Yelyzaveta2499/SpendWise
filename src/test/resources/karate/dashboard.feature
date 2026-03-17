Feature: Dashboard API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: view dashboard overview
    Given path 'api', 'dashboard'
    When method get
    Then status 200
    And match response == { income: '#number', expenses: '#number', period: '#string' }

  Scenario: select different time period
    Given path 'api', 'dashboard'
    And param period = '2026-02'
    When method get
    Then status 200
    And match response.period == '2026-02'

  Scenario: empty dashboard state
    Given path 'api', 'dashboard'
    And param period = '2025-01'
    When method get
    Then status 200
    And match response.expenses == 0

