package ru.disdev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class PaymentHandlerApplication {

	@Autowired
	private Environment environment;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(environment.getRequiredProperty("mail.server"));
		javaMailSender.setPort(environment.getRequiredProperty("mail.port", Integer.class));
		javaMailSender.setUsername(environment.getRequiredProperty("mail.user"));
		javaMailSender.setPassword(environment.getRequiredProperty("mail.password"));
		javaMailSender.setDefaultEncoding("UTF-8");
		Properties properties = new Properties();
		properties.put("mail.transport.protocol", environment.getRequiredProperty("mail.protocol"));
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", environment.getRequiredProperty("mail.enable.tsl", Boolean.class));
		javaMailSender.setJavaMailProperties(properties);
		return javaMailSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(PaymentHandlerApplication.class, args);
	}
}
