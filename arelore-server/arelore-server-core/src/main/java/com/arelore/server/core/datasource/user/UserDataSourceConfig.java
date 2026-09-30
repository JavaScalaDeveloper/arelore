package com.arelore.server.core.datasource.user;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.user", name = "url")
@MapperScan(
    basePackages = {
        "com.arelore.server.core.registration.mapper",
        "com.arelore.server.core.biz.user.mapper"
    },
    sqlSessionTemplateRef = "userSqlSessionTemplate"
)
public class UserDataSourceConfig {

    @Bean(name = "userDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.user")
    public DataSourceProperties userDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "userDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.user.hikari")
    public DataSource userDataSource(
        @Qualifier("userDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "userSqlSessionFactory")
    public SqlSessionFactory userSqlSessionFactory(
        @Qualifier("userDataSource") DataSource dataSource,
        MybatisPlusInterceptor mybatisPlusInterceptor
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // 自定义 SqlSessionFactory 不会自动挂插件；不设置则 selectPage 无 LIMIT，pageSize 失效
        factoryBean.setPlugins(mybatisPlusInterceptor);
        return factoryBean.getObject();
    }

    @Bean(name = "userSqlSessionTemplate")
    public SqlSessionTemplate userSqlSessionTemplate(@Qualifier("userSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "userTransactionManager")
    public DataSourceTransactionManager userTransactionManager(@Qualifier("userDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

