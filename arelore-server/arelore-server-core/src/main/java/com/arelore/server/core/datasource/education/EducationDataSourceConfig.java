package com.arelore.server.core.datasource.education;

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
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.education", name = "url")
@MapperScan(
    basePackages = {
        "com.arelore.server.core.detection.mapper",
        "com.arelore.server.core.biz.word.admin.mapper",
        "com.arelore.server.core.biz.word.user.mapper"
    },
    sqlSessionTemplateRef = "educationSqlSessionTemplate"
)
public class EducationDataSourceConfig {

    @Bean(name = "educationDataSourceProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.education")
    public DataSourceProperties educationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "educationDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.education.hikari")
    public DataSource educationDataSource(
        @Qualifier("educationDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "educationSqlSessionFactory")
    @Primary
    public SqlSessionFactory educationSqlSessionFactory(@Qualifier("educationDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean(name = "educationSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate educationSqlSessionTemplate(@Qualifier("educationSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "educationTransactionManager")
    @Primary
    public DataSourceTransactionManager educationTransactionManager(@Qualifier("educationDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

