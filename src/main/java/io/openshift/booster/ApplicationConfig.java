package io.openshift.booster;

import io.openshift.booster.service.DatabaseConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties()
public class ApplicationConfig {


    private DatabaseConfig database;

    public ApplicationConfig() {
    }

    public ApplicationConfig(final DatabaseConfig database) {
        this.database = database;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public ApplicationConfig setDatabase(final DatabaseConfig database) {
        this.database = database;
        return this;
    }
}
