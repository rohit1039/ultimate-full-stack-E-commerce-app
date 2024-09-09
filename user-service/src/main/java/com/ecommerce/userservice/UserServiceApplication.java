package com.ecommerce.userservice;

import java.util.concurrent.Executor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class UserServiceApplication {

  public static void main(String[] args) {

    SpringApplication.run(UserServiceApplication.class, args);
  }

  @Bean
  public ModelMapper modelMapper() {

    return new ModelMapper();
  }
}
