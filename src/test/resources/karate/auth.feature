Feature: Auth API

  @Login-success
  Scenario: login with valid credentials
    Given url baseUrl
    And path 'login'
    And form field 'username' = 'indiv'
    And form field 'password' = 'password123'
    When method post
    Then status 200
    * def cookies = responseCookies
    * def result = { cookies: cookies }
    * karate.set('result', result)
    * match cookies != null

