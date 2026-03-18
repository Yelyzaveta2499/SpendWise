Feature: Auth API

  Background:
    * def username = karate.get('username') || 'indiv'
    * def password = karate.get('password') || 'password'

  @login-success
  Scenario: login with valid credentials
    Given url baseUrl
    And path 'login'
    And form field 'username' = username
    And form field 'password' = password
    When method post
    Then status 200
    * def cookies = responseCookies
    * if (username == 'business') cookies.userType = { value: 'business' }
    * if (username != 'business') cookies.userType = { value: 'indiv' }
    * def result = { cookies: cookies }
    * karate.set('result', result)
    * match cookies != null

