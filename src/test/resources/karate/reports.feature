Feature: Reports API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: view financial reports
    Given path 'api', 'reports'
    When method get
    Then status 200
    And match response.charts == '#present'

  Scenario: select reporting period
    Given path 'api', 'reports'
    And param period = '2026-02'
    When method get
    Then status 200
    And match response.period == '2026-02'

  Scenario: empty reports state
    Given path 'api', 'reports'
    And param period = '2025-01'
    When method get
    Then status 200
    And match response.charts == null

