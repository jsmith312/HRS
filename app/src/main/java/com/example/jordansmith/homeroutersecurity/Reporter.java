package com.example.jordansmith.homeroutersecurity;

public class Reporter {
    private boolean hasDefaultPassword;
    private String Company;
    private String defaultUserName;
    private String defaultPassword;

    public Reporter(boolean hasDefaultPassword, String company, String defaultUserName, String defaultPassword) {
        this.hasDefaultPassword = hasDefaultPassword;
        this.Company = company;
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
    }

    public boolean getHasDefaultPassword() {
        return hasDefaultPassword;
    }

    public void setHasDefaultPassword(boolean hasDefaultPassword) {
        this.hasDefaultPassword = hasDefaultPassword;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

}
