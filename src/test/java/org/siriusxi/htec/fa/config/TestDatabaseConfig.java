package org.siriusxi.htec.fa.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

/**
 * Test configuration to ensure proper H2 in-memory database setup
 */
@TestConfiguration
@Profile("test")
public class TestDatabaseConfig {

    @Bean
    @Primary
    public DataSource testDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}