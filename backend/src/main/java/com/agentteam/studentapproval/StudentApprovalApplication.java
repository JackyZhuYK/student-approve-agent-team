package com.agentteam.studentapproval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableCaching
@EnableSpringDataWebSupport
public class StudentApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentApprovalApplication.class, args);
    }
}
