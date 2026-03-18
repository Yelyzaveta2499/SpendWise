Feature: Account Settings API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: update account settings
    * def settings = { notifications: true, theme: 'dark' }
    Given path 'api', 'settings'
    And request settings
    When method put
    Then status 200
    And match response.notifications == true
    And match response.theme == 'dark'

  Scenario: enable preferences
    * def preferences = { autoSave: true }
    Given path 'api', 'settings', 'preferences'
    And request preferences
    When method put
    Then status 200
    And match response.autoSave == true

  Scenario: error on invalid input
    * def settings = { notifications: 'invalid' }
    Given path 'api', 'settings'
    And request settings
    When method put
    Then status 400

