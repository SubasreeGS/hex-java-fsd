package com.ecommerce.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.ecommerce")

public class AppConfig {
    @Bean
    public DataSource getDataSource()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/ecommerce");
        config.setUsername("root");
        config.setPassword("Suba@2011");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate getTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

}
