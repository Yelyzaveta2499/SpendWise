Feature: Budgets API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: create budget for category
    * def budget = { category: 'Food', amount: 200, month: '2026-03' }
    Given path 'api', 'budgets'
    And request budget
    When method post
    Then status 201
    And match response.category == budget.category

  Scenario: view budgets
    Given path 'api', 'budgets'
    When method get
    Then status 200
    And match each response == { category: '#string', amount: '#number', month: '#string' }

  Scenario: reject negative budget amount
    * def budget = { category: 'Food', amount: -100, month: '2026-03' }
    Given path 'api', 'budgets'
    And request budget
    When method post
    Then status 400


