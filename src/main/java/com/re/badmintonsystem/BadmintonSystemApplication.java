package com.re.badmintonsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BadmintonSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonSystemApplication.class, args);
    }

}
