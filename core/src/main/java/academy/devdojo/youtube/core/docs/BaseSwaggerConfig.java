package academy.devdojo.youtube.core.docs;

import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class BaseSwaggerConfig {
    private final String basePackage;

    public BaseSwaggerConfig(String basePackage){
        this.basePackage = basePackage;
    }

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .build()
                .apiInfo(metaData());
    }

    private ApiInfo metaData(){
        return new ApiInfoBuilder()
                .title("Another Awesome course from DevDojo Spring Boot Microservices")
                .description("Everybody is a Jedi noew")
                .version("1.0")
                .contact(new Contact("Julio Oliveira", "https://joliveira.com.br", "joliveira.net@gmail.com"))
                .license("Private stuff bro, belongs to DevDojo")
                .licenseUrl("http://devdojo.academy")
                .build();
    }
}
