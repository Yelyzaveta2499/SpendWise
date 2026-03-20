@mock
Feature: SpendWise mock API

  Background:
    * def state = karate.get('state')
    * if (!state) karate.set('state', { nextExpenseId: 3, expenses: { '1': { id: 1, name: 'Groceries Weekly', category: 'Food', amount: 25.50, date: '2026-03-10', tags: ['travel'] }, '2': { id: 2, name: 'Groceries Market', category: 'Food', amount: 40.00, date: '2026-03-12', tags: ['travel', 'client'] } }, budgets: [{ category: 'Food', amount: 200, month: '2026-03' }], users: [{ id: 1, username: 'indiv', email: 'indiv@example.com' }, { id: 2, username: 'business', email: 'business@example.com' }] })
    * def state = karate.get('state')

  Scenario: pathMatches('/login') && methodIs('post')
    * def req = karate.get('request')
    * def params = karate.get('requestParams')
    * def username = req && req.username ? req.username : (params && params.username ? (Array.isArray(params.username) ? params.username[0] : params.username) : 'indiv')
    * def password = req && req.password ? req.password : (params && params.password ? (Array.isArray(params.password) ? params.password[0] : params.password) : 'password')
    * def userType = username == 'business' ? 'business' : 'indiv'
    * def sessionValue = username == 'business' ? 'business-session' : (password == 'password123' ? 'indiv-restricted-session' : 'indiv-session')
    * def responseCookies = { JSESSIONID: { value: sessionValue }, userType: { value: userType } }
    * def response = { message: 'Login successful' }
    * def responseStatus = 200

  Scenario: pathMatches('/api/expenses') && methodIs('get')
    * def allExpenses = Object.keys(state.expenses).map(function(k){ return state.expenses[k] })
    * def first = function(v){ return v ? (Array.isArray(v) ? v[0] : v) : null }
    * def search = requestParams ? first(requestParams.search) : null
    * def category = requestParams ? first(requestParams.category) : null
    * def tag = requestParams ? first(requestParams.tag) : null
    * def sort = requestParams ? first(requestParams.sort) : null
    * def result = allExpenses
    * if (search) result = karate.filter(result, function(x){ return x.name.indexOf(search) > -1 })
    * if (category) result = karate.filter(result, function(x){ return x.category == category })
    * if (tag) result = karate.filter(result, function(x){ return x.tags && x.tags.indexOf(tag) > -1 })
    * if (sort == 'amount') result = result.sort(function(a, b){ return a.amount - b.amount })
    * if (!search && !category && !tag && !sort) result = karate.map(result, function(x){ return { id: x.id, name: x.name, amount: x.amount, date: x.date } })
    * def response = result
    * def responseStatus = 200

  Scenario: pathMatches('/api/expenses') && methodIs('post')
    * def id = state.nextExpenseId
    * state.nextExpenseId = id + 1
    * def created = request
    * set created.id = id
    * set created.tags = []
    * state.expenses['' + id] = created
    * karate.set('state', state)
    * def response = created
    * def responseStatus = 201

  Scenario: pathMatches('/api/expenses/{id}') && methodIs('get')
    * def expense = state.expenses[pathParams.id]
    * if (!expense) karate.abort()
    * def response = expense
    * def responseStatus = 200

  Scenario: pathMatches('/api/expenses/{id}') && methodIs('delete')
    * def responseStatus = 204

  Scenario: pathMatches('/api/expenses/{id}/tags') && methodIs('post')
    * def forbidden = request && request.length == 1
    * def expense = state.expenses[pathParams.id] || { id: +pathParams.id, name: 'Tagged Expense', category: 'Travel', amount: 30, date: '2026-03-17', tags: [] }
    * if (!forbidden) expense.tags = request
    * if (!forbidden) state.expenses[pathParams.id] = expense
    * karate.set('state', state)
    * def response = forbidden ? { message: 'Forbidden' } : { id: expense.id, tags: expense.tags }
    * def responseStatus = forbidden ? 403 : 200

  Scenario: pathMatches('/api/budgets') && methodIs('post')
    * def badRequest = request.amount < 0
    * if (!badRequest) state.budgets.push({ category: request.category, amount: request.amount, month: request.month })
    * karate.set('state', state)
    * def response = badRequest ? { error: 'Amount must be non-negative' } : { category: request.category, amount: request.amount, month: request.month }
    * def responseStatus = badRequest ? 400 : 201

  Scenario: pathMatches('/api/budgets') && methodIs('get')
    * def response = state.budgets
    * def responseStatus = 200

  Scenario: pathMatches('/api/goals') && methodIs('post')
    * def badRequest = request.target < 0
    * def response = badRequest ? { error: 'Invalid target' } : { id: 1, name: request.name, target: request.target, progress: request.progress }
    * def responseStatus = badRequest ? 400 : 201

  Scenario: pathMatches('/api/goals/{id}/contributions') && methodIs('post')
    * def response = { id: '#(parseInt(pathParams.id))', progress: '#(100 + request.amount)' }
    * def responseStatus = 200

  Scenario: pathMatches('/api/reports') && methodIs('get')
    * def period = requestParams && requestParams.period ? (Array.isArray(requestParams.period) ? requestParams.period[0] : requestParams.period) : '2026-03'
    * def response = period == '2025-01' ? { period: period, charts: null } : { period: period, charts: { expensesByCategory: [10, 20] } }
    * def responseStatus = 200

  Scenario: pathMatches('/api/settings') && methodIs('put')
    * def badRequest = request.notifications != null && typeof request.notifications != 'boolean'
    * def response = badRequest ? { error: 'Invalid input' } : request
    * def responseStatus = badRequest ? 400 : 200

  Scenario: pathMatches('/api/settings/preferences') && methodIs('put')
    * def response = request
    * def responseStatus = 200

  Scenario: pathMatches('/api/users') && methodIs('get')
    * def response = state.users
    * def responseStatus = 200

  Scenario: pathMatches('/api/users/{id}') && methodIs('get')
    * def response = { id: '#(parseInt(pathParams.id))', username: 'indiv', email: 'indiv@example.com' }
    * def responseStatus = 200

  Scenario: pathMatches('/api/users/{id}') && methodIs('put')
    * def response = { id: '#(parseInt(pathParams.id))', username: 'indiv', email: '#(request.email)' }
    * def responseStatus = 200

  Scenario: pathMatches('/api/dashboard') && methodIs('get')
    * def period = requestParams && requestParams.period ? (Array.isArray(requestParams.period) ? requestParams.period[0] : requestParams.period) : '2026-03'
    * def response = period == '2025-01' ? { income: 0, expenses: 0, period: period } : { income: 5000, expenses: 1200, period: period }
    * def responseStatus = 200

  Scenario:
    * def response = { error: 'Not found', path: '#(requestPath)' }
    * def responseStatus = 404


