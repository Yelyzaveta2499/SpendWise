Feature: Expenses Search/Filter/Sort API

  Background:
    * def loginResult = call read('classpath:karate/auth.feature@login-success') { username: 'indiv', password: 'password' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: search expenses by keyword
    Given path 'api', 'expenses'
    And param search = 'Groceries'
    When method get
    Then status 200
    And match each response.name contains 'Groceries'

  Scenario: filter expenses by category
    Given path 'api', 'expenses'
    And param category = 'Food'
    When method get
    Then status 200
    And match each response.category == 'Food'

  Scenario: sort expenses by amount
    Given path 'api', 'expenses'
    And param sort = 'amount'
    When method get
    Then status 200
    And match response[0].amount <= response[1].amount

  Scenario: empty search result
    Given path 'api', 'expenses'
    And param search = 'NonExistent'
    When method get
    Then status 200
    And match response == '#[]'

