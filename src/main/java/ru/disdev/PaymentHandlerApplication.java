package ru.disdev;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;
import ru.disdev.commands.ByeCommand;
import ru.disdev.commands.HelloCommand;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
@PropertySource("file:application.yaml")
public class PaymentHandlerApplication {

	@Autowired
    private ru.disdev.Properties properties;

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean
    public CommandRegistry commandRegistry() {
        return new CommandRegistry(false, properties.botName);
    }

    @Bean
    public BotCommand helloCommand() {
        return new HelloCommand("hello", "Инициализация бота");
    }

    @Bean
    public BotCommand byeCommand() {
        return new ByeCommand("bye", "Открепляет бота от текущего чата");
    }

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(properties.mailServer);
        javaMailSender.setPort(properties.mailPort);
        javaMailSender.setUsername(properties.mailUser);
        javaMailSender.setPassword(properties.mailPassword);
        javaMailSender.setDefaultEncoding("UTF-8");
		Properties properties = new Properties();
        properties.put("mail.transport.protocol", this.properties.mailProtocol);
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", this.properties.mailEnableTsl);
        javaMailSender.setJavaMailProperties(properties);
		return javaMailSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(PaymentHandlerApplication.class, args);
	}
}
