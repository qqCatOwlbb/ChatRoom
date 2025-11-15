package com.catowl.chatroom.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @program: ChatRoom
 * @description: SpringDoc OpenAPI (Swagger 3) 配置
 * @author: qqCatOwlbb
 * @create: 2025-11-15 17:58
 **/
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 1. 定义项目基本信息
                .info(new Info()
                        .title("ChatRoom API")
                        .description("ChatRoom-聊天室项目 API 文档")
                        .version("v0.1"))

                // 2. 添加全局安全认证组件
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP) // 类型
                                        .scheme("bearer")               // scheme
                                        .bearerFormat("JWT")            // 格式
                                        .description("输入你的 AccessToken (JWT)")))

                // 3. 添加全局的安全需求
                //    (所有接口都会显示一个锁，但只在 Spring Security 配置了 authenticated() 的接口上才真正生效)
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}
