package com.xenon.ows.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OwsWebConfig {

    @Bean
    public FilterRegistrationBean<CaseInsensitiveRequestFilter> caseInsensitiveRequestFilter() {
        FilterRegistrationBean<CaseInsensitiveRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CaseInsensitiveRequestFilter());
        registrationBean.addUrlPatterns("/services/*"); // Broad pattern, filter logic limits logic execution inside
        registrationBean.setOrder(1); // Low order to execute early, but after Spring encoding filters potentially
        return registrationBean;
    }
}
