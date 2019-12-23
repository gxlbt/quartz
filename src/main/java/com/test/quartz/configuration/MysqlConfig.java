package com.test.quartz.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author xqli7@iflytek.com
 * @date 2019/2/13
 * @description: mysql连接配置
 */
@Component
public class MysqlConfig {

    @Bean
    public DataSource dataSource() {

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/quartz?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai");
        hikariDataSource.setUsername("root");
        hikariDataSource.setPassword("root");
        hikariDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariDataSource.setPoolName("quartz");
//      连接超时时间
        hikariDataSource.setConnectionTimeout(30000);
//      最大池大小
        hikariDataSource.setMaximumPoolSize(20);
//      默认连接数
        hikariDataSource.setMinimumIdle(10);
        hikariDataSource.setIdleTimeout(600000);
        hikariDataSource.setMaxLifetime(1800000);

        return hikariDataSource;
    }
}
