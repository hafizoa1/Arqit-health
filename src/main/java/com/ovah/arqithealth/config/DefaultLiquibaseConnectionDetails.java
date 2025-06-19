package com.ovah.arqithealth.config;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

public class DefaultLiquibaseConnectionDetails implements LiquibaseConnectionDetails {

    private final LiquibaseProperties properties;

    public DefaultLiquibaseConnectionDetails(LiquibaseProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getUsername() {
        return properties.getUser();
    }

    @Override
    public String getPassword() {
        return properties.getPassword();
    }

    @Override
    public String getJdbcUrl() {
        return properties.getUrl();
    }
}
