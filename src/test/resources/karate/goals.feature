Feature: Savings Goals API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: create savings goal
    * def goal = { name: 'Vacation', target: 1000, progress: 0 }
    Given path 'api', 'goals'
    And request goal
    When method post
    Then status 201
    And match response.name == goal.name

  Scenario: add contribution to goal
    * def goalId = 1
    * def contribution = { amount: 100 }
    Given path 'api', 'goals', goalId, 'contributions'
    And request contribution
    When method post
    Then status 200
    And assert response.progress >= 100

  Scenario: reject invalid target amount
    * def goal = { name: 'Vacation', target: -500, progress: 0 }
    Given path 'api', 'goals'
    And request goal
    When method post
    Then status 400

