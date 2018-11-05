package com.exshell;

import com.exshell.base.AppInfo;
import com.exshell.base.utils.OrderPriceUtil;
import com.exshell.common.service.AddressTypeConfiguration;
import com.exshell.config.SSOConfiguration;
import com.exshell.crypto.signature.SignatureConfigs;
import com.exshell.dawn.channel.DwChannelOutput;
import com.exshell.uc.SSOClient;
import com.exshell.uc.SSOMessageListener;
import com.exshell.utils.PeerClient;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;


/**
 * Application entry.
 */
@EnableSwagger2
@EnableScheduling
@EnableCaching
@EnableAspectJAutoProxy
@EnableConfigurationProperties({
        SignatureConfigs.class,
        AddressTypeConfiguration.class,
})
@EnableBinding({
        DwChannelOutput.class, AppInfo.class
})
@SpringBootApplication
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = {SSOConfiguration.class, SSOClient.class, SSOMessageListener.class, OrderPriceUtil.class, PeerClient.class}
                )
        }
)
public class MainApplication {

  public static void main(String[] args) {
    SpringApplication.run(MainApplication.class, args);
  }

  @Autowired
  private AppInfo appInfo;

  @Bean
  public Docket createSwaggerDocket() {
    return new Docket(DocumentationType.SWAGGER_2)
            .securitySchemes(singletonList(apiKey()))
            .securityContexts(singletonList(securityContext()))
            .apiInfo(apiInfo()).select()
            .paths(acceptPath("/internal/**", "/otc/**", "/v1/act/**"))
            .build();
  }

  private ApiKey apiKey() {
    return new ApiKey("EXSHELL-MAIN-TOKEN", "Token", "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build();
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[] { authorizationScope };
    return singletonList(new SecurityReference("EXSHELL-MAIN-TOKEN", authorizationScopes));
  }

  private Predicate<String> acceptPath(String... patterns) {
    return Predicates.or(Arrays.stream(patterns).map((s) -> {
      return PathSelectors.ant(s);
    }).collect(Collectors.toList()));
  }

  private ApiInfo apiInfo() {
    Contact contact = new Contact("Exshell", "https://www.exshell.com", "tech@exshell.com");
    return new ApiInfo(appInfo.name, appInfo.appDescription(), "v1.0",
            "https://www.exshell.com/p/about/about_law", contact, "Commercial",
            "https://www.exshell.com/p/about/about_law");
  }

}
