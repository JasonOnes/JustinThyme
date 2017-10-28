
package com.JustinThyme.justinthymer.hello;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/com/JustinThyme/justinthymer").setViewName("com/JustinThyme/justinthymer");
        registry.addViewController("/JustinThyme/login").setViewName("login");
        registry.addViewController("/JustinThyme/welcome-user").setViewName("welcome-user");
        //registry.addViewController("/JustinThyme/signup").setViewName("signup");
    }
}
