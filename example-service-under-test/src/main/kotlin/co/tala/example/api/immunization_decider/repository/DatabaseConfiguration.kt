package co.tala.example.api.immunization_decider.repository

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManagerFactory

@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@EnableJpaRepositories(
    entityManagerFactoryRef = "ImmunizationDecisionEntityManagerFactory",
    transactionManagerRef = "ImmunizationDecisionTransactionManager",
    basePackages = ["co.tala.example.api.immunization_decider.repository"]
)
class DatabaseConfiguration {

    @Bean(name = ["ImmunizationDecisionDataSource"])
    @ConfigurationProperties(prefix = "immunizationdecider.datasource")
    fun datasource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean(name = ["ImmunizationDecisionEntityManagerFactory"])
    fun creditBureauEntityManagerFactory(
        builder: EntityManagerFactoryBuilder, @Qualifier("ImmunizationDecisionDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return builder.dataSource(dataSource).packages("co.tala.example.api.immunization_decider.repository.model")
            .persistenceUnit("immunization_decider")
            .build()
    }

    @Bean(name = ["ImmunizationDecisionTransactionManager"])
    fun transactionManager(
        @Qualifier("ImmunizationDecisionEntityManagerFactory") ImmunizationDecisionEntityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(ImmunizationDecisionEntityManagerFactory)
    }
}
