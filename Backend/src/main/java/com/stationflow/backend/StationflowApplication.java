package com.stationflow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class StationflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(StationflowApplication.class, args);
	}

}
