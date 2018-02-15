package io.openshift.booster.service;

public class DatabaseConfig {


    private String url;

    private String username;
    private String password;

    private String database;

    public DatabaseConfig() {
    }

    public DatabaseConfig(final String url, final String username, final String password, final String database) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public DatabaseConfig setUrl(final String url) {
        this.url = url;
        return this;
    }

    public DatabaseConfig setUsername(final String username) {
        this.username = username;
        return this;
    }

    public DatabaseConfig setPassword(final String password) {
        this.password = password;
        return this;
    }

    public DatabaseConfig setDatabase(final String database) {
        this.database = database;
        return this;
    }
}
