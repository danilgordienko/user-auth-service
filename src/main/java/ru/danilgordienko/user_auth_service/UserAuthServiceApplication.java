package ru.danilgordienko.user_auth_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserAuthServiceApplication {

	public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.load();
//		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
//		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//		System.setProperty("DB_SOURCE", dotenv.get("DB_SOURCE"));
//		System.setProperty("APP_SECURITY_REFRESH_EXPIRATION", dotenv.get("APP_SECURITY_REFRESH_EXPIRATION"));
//		System.setProperty("APP_SECURITY_ACCESS_EXPIRATION", dotenv.get("APP_SECURITY_ACCESS_EXPIRATION"));
//		System.setProperty("DB_SOURCE", dotenv.get("DB_SOURCE"));

		SpringApplication.run(UserAuthServiceApplication.class, args);
	}

}
