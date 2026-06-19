package com.beyond.qiin.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

// rag 전용 구성 클래스 - bean들을 spring container에 등록
@Configuration
@EnableJpaRepositories(
        basePackages = "com.beyond.qiin.domain.rag.repository", // rag.repsitory 의 위치는 해당 설정을 따름
        entityManagerFactoryRef = "ragEntityManagerFactory",
        transactionManagerRef = "ragTransactionManager")
public class RagPostgresJpaConfig {

    @Bean
    @ConfigurationProperties("rag.datasource") // application.yml의 rag.datasource의 내용이 DataSourceProperties로 binding
    public DataSourceProperties ragDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("rag.datasource.hikari") // hikari 세부 설정
    public HikariDataSource ragDataSource(
            // datasource 접속정보로 실제 postgresql 연결 객체를 만들어줌
            // JpaRepositoryConfig로 인해 동일 bean 생기므로 구분자 활용해 전달
            @Qualifier("ragDataSourceProperties") DataSourceProperties ragDataSourceProperties) {
        return ragDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // emf - jpa가 어떤 db, entity, hikari 설정을 사용하는지 결정
    @Bean
    public LocalContainerEntityManagerFactoryBean ragEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("ragDataSource") HikariDataSource ragDataSource, // rag 전용 postgresql 연결
            @Value("${rag.jpa.hibernate.ddl-auto:validate}")
                    String ddlAuto, // default auto x 시 validate(schema 확인, 수정 x)
            @Value("${rag.jpa.database-platform:org.hibernate.dialect.PostgreSQLDialect}") String dialect,
            @Value("${rag.flyway.enabled:true}") boolean flywayEnabled,
            @Value("${rag.flyway.locations:classpath:db/migration/postgresql}")
                    String flywayLocations, // migration file 경로
            @Value("${rag.flyway.baseline-on-migrate:true}") boolean baselineOnMigrate,
            @Value("${rag.flyway.baseline-version:0}") String baselineVersion) {

        // jpa 시작 전 migration 진행
        // ddl-auto=validate인 경우 table이 먼저 존재해야하기 때문
        // postgresql 연결 - flyway sql 실행 - table 생성 및 변경 - hibernate validate - emf 생성 순
        migrateRagDatabase(ragDataSource, flywayEnabled, flywayLocations, baselineOnMigrate, baselineVersion);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", dialect); // postgresql에 맞는 sql 문법 적용

        return builder.dataSource(ragDataSource)
                .packages("com.beyond.qiin.domain.rag.entity") // rag folder entity scan
                .persistenceUnit("ragPostgresPersistenceUnit") // DB를 사용할 때 JPA 단위를 구분하기 위한 식별자
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager ragTransactionManager(
            @Qualifier("ragEntityManagerFactory") EntityManagerFactory ragEntityManagerFactory) {
        return new JpaTransactionManager(ragEntityManagerFactory);
    }

    // flyway 실행용
    private void migrateRagDatabase(
            HikariDataSource ragDataSource,
            boolean flywayEnabled,
            String flywayLocations,
            boolean baselineOnMigrate,
            String baselineVersion) {

        // flyway 비활성화된 경우
        if (!flywayEnabled) {
            return;
        }

        Flyway.configure()
                .dataSource(ragDataSource)
                .locations(flywayLocations)
                .baselineOnMigrate(baselineOnMigrate) // true: 이미 table 있고 이력에 없는 경우의 db도 flyway 도입 가능
                .baselineVersion(baselineVersion) // 기존 db 상태를 버전으로 정의
                .load()
                .migrate();
    }
}
