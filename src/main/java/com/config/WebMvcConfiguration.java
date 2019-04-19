package com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * User: PK
 * Date: 2019/4/19
 * Time: 9:35
 */

    @Configuration
    public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
/*            registry.addResourceHandler("/image*//**").addResourceLocations("file:D://User/");
            super.addResourceHandlers(registry);*/
        }


    }
