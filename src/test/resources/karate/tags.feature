Feature: Expense Tagging API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'business', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: assign tags to expense
    * def expenseId = 1
    * def tags = ['travel', 'client']
    Given path 'api', 'expenses', expenseId, 'tags'
    And request tags
    When method post
    Then status 200
    And match response.tags contains 'travel'

  Scenario: filter expenses by tag
    Given path 'api', 'expenses'
    And param tag = 'travel'
    When method get
    Then status 200
    And match each response.tags contains 'travel'

  Scenario: block tagging for Individual account
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password123' }
    * configure cookies = loginResult.cookies
    * url baseUrl
    * def expenseId = 1
    * def tags = ['travel']
    Given path 'api', 'expenses', expenseId, 'tags'
    And request tags
    When method post
    Then status 403

