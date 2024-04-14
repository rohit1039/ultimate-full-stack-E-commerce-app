package com.ecommerce.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {

    registry
        .addResourceHandler("/user-images/**")
        .addResourceLocations("file:" + System.getProperty("user.dir") + "/user-images/");
  }

  @Bean
  public ObjectMapper getObjectMapper() {

    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return mapper;
  }

  @Bean
  public JavaMailSender getJavaMailSender() {

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(587);
    mailSender.setUsername("wearinshopping.services@gmail.com");
    mailSender.setPassword("ykix uqxr myvu nuzv");
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");
    return mailSender;
  }
}
