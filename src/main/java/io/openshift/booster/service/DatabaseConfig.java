package io.openshift.booster.service;

public class DatabaseConfig {


    private String url;

    private String userName;
    private String password;

    private String databaseName;

    public DatabaseConfig() {
    }

    public DatabaseConfig(final String url, final String userName, final String password, final String databaseName) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.databaseName = databaseName;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public DatabaseConfig setUrl(final String url) {
        this.url = url;
        return this;
    }

    public DatabaseConfig setUserName(final String userName) {
        this.userName = userName;
        return this;
    }

    public DatabaseConfig setPassword(final String password) {
        this.password = password;
        return this;
    }

    public DatabaseConfig setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return this;
    }
}
