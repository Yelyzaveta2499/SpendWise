Feature: Expenses API

  Background:
    * def loginResult = call read('classpath:features/karate/auth.feature@login-success') { username: 'indiv', password: 'password123' }
    * configure cookies = loginResult.cookies
    * url baseUrl

  Scenario: list expenses
    Given path 'api', 'expenses'
    When method get
    Then status 200
    And match response == '#[]'  # Adjust as needed for your data
    And match each response == { id: '#number', name: '#string', amount: '#number', date: '#string' }

  Scenario: create expense
    * def expense = { name: 'Test Karate Expense', category: 'Test', amount: 12.34, date: '2026-03-17' }
    Given path 'api', 'expenses'
    And request expense
    When method post
    Then status 201
    * def expenseId = response.id
    And match response.name == expense.name

  Scenario: get created expense
    * def expense = { name: 'Test Karate Expense', category: 'Test', amount: 12.34, date: '2026-03-17' }
    * def createResult = callonce read('classpath:features/karate/expenses.feature@create expense')
    * def expenseId = createResult.expenseId
    Given path 'api', 'expenses', expenseId
    When method get
    Then status 200
    And match response.name == expense.name

  Scenario: delete expense
    * def createResult = callonce read('classpath:features/karate/expenses.feature@create expense')
    * def expenseId = createResult.expenseId
    Given path 'api', 'expenses', expenseId
    When method delete
    Then status 204

