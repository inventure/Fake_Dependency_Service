package co.tala.api.fakedependency.swagger

import co.tala.api.fakedependency.constant.Constant.MOCK_SERVICE
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun fakeDependencyApi() = docket("Fake Dependency Controller", "$MOCK_SERVICE.*")

    private fun docket(groupName: String, regex: String) =
        Docket(DocumentationType.SWAGGER_2)
            .groupName(groupName)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.regex(regex))
            .build()
            .apiInfo(metaData())

    private fun metaData(): ApiInfo {
        return ApiInfoBuilder()
            .title("Fake Dependency Service REST API")
            .description("REST API for mocking any API")
            .version("1.0.0")
            .contact(Contact("Elliot Masor", "", ""))
            .build()
    }
}
