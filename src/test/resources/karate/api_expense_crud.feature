Feature: Expense API CRUD

  Background:
    * url baseUrl

  Scenario: Create, retrieve, and delete an expense
    # Create expense
    Given path 'api/expenses'
    And request { name: 'Test Karate', category: 'Test', amount: 99.99, date: '2026-03-17' }
    When method post
    Then status 201
    * def expenseId = response.id

    # Retrieve expense
    Given path 'api/expenses', expenseId
    When method get
    Then status 200
    And match response.name == 'Test Karate'

    # Delete expense
    Given path 'api/expenses', expenseId
    When method delete
    Then status 204

