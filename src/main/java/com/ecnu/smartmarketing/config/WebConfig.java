//package com.ecnu.smartmarketing.config;
//
//import com.ecnu.smartmarketing.filter.AccessInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.Arrays;
//
///**
// * @author yoon
// * @description: TODO
// * @date 2022/6/1421:16
// */
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // 自定义拦截器
//        registry.addInterceptor(new AccessInterceptor()).addPathPatterns("/**");
//    }
//}
