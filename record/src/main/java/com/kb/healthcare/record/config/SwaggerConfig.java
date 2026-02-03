package com.kb.healthcare.record.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
        ;
    }

    public Info apiInfo() {
        return new Info()
                .title("KB Healthcare")
                .description("건강 데이터 관리 API 문서")
                .version("1.0.0")
                .contact(
                        new Contact()
                                .name("송정훈")
                                .email("thdwjdgns0@naver.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
        ;
    }

    public List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:9001"))
        ;
    }

}
