package com.library.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.library")
@EnableTransactionManagement
public class AppConfig {

    @Bean
    public DataSource getDatasource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:mysql://localhost:3306/library_management",
                "root",
                "Suba@2011"
        );
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityFactory(){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean
                = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(getDatasource());

        entityManagerFactoryBean.setPackagesToScan("com.library.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();

        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        entityManagerFactoryBean.setJpaProperties(properties);
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager getTransactionManager(){
        return new JpaTransactionManager(getEntityFactory().getObject());
    }

}
