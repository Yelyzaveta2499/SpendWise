Feature: Users API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: list users
    Given path 'api', 'users'
    When method get
    Then status 200
    And match each response == { id: '#number', username: '#string', email: '#string' }

  Scenario: get user by id
    * def userId = 1
    Given path 'api', 'users', userId
    When method get
    Then status 200
    And match response.id == userId
    And match response.username == '#string'

  Scenario: update user
    * def userId = 1
    * def update = { email: 'newemail@example.com' }
    Given path 'api', 'users', userId
    And request update
    When method put
    Then status 200
    And match response.email == update.email
