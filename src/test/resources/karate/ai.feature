Feature: AI Insights API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: request AI insights
    Given path 'api', 'ai', 'insights'
    When method get
    Then status 200
    And match response.summary == '#string'

  Scenario: insight tone matches account type
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'business', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl
    Given path 'api', 'ai', 'insights'
    When method get
    Then status 200
    And match response.tone == 'business'

  Scenario: fallback when AI unavailable
    Given path 'api', 'ai', 'insights'
    And param ai = 'off'
    When method get
    Then status 200
    And match response.summary == 'AI unavailable'

