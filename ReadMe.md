# SpendWise - User Guide

This short guide explains how to start and use the SpendWise app. It will be updated with screenshots later.

## 1. Prerequisites
- Java 21 or newer installed
- Maven installed
- MySQL running locally

## 2. Setup
- Open `application.properties` and check the database settings:
  - URL: `jdbc:mysql://localhost:3306/spendwisedb?createDatabaseIfNotExist=true`
  - Username/Password: `root` / `root`
- Roles are preloaded (INDIVIDUAL, BUSINESS, KIDS) using `data.sql`.

## 3. Run the App
```powershell
mvn spring-boot:run
```
- App starts on `http://localhost:1111`

## 4. Login
- You’ll see Spring Security’s default login page.
- Use one of the in-memory users defined in `SecurityConfig`.
  - Examples:
    - Username: `user`, Password: `password`
    - Username: `admin`, Password: `admin123`
    - Or any test user configured in tests

## 5. Navigation
- After login, you’ll land on the main dashboard (`index.html`).
- Sidebar links switch sections while keeping the main layout.

## 6. Troubleshooting
- If the login page doesn’t show, ensure Spring Security is enabled and that `/login` isn’t overridden.
- If the DB won’t connect, verify MySQL is running and credentials match `application.properties`.
- If roles are missing, make sure `data.sql` executed or set:
```ini
spring.sql.init.mode=always
```

## 7. Next Features (Future)
- Manage users and account types in the UI.
- Database-backed authentication.
- Role-based access control.

This guide will include screenshots in future updates.

