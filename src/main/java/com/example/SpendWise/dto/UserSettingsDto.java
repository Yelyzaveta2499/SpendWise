package com.example.SpendWise.dto;


public class UserSettingsDto {

    private String firstName;
    private String lastName;
    private String email;
    private String currency;   // e.g. "USD"
    private String accountType; // e.g. "PERSONAL" or "BUSINESS"

    public UserSettingsDto() {
        // Default constructor required for frameworks (e.g., Jackson, JPA)
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

