package com.ovah.arqithealth.config;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

@Configuration
@Profile("hospital-a")
public class HospitalAConfig {

    @Bean
    public LiquibaseConnectionDetails liquibaseConnectionDetailsForPostgresHospitalA(LiquibaseProperties properties) {
        return new DefaultLiquibaseConnectionDetails(properties);
    }
}