package com.luc.raizesdeserto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RaizesDesertoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaizesDesertoApplication.class, args);
    }

}
