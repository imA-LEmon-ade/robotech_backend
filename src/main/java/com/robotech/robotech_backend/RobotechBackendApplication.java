package com.robotech.robotech_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RobotechBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobotechBackendApplication.class, args);
	}

}
