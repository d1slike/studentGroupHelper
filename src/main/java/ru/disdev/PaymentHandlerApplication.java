package ru.disdev;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;
import ru.disdev.commands.TimeTableCommand;
import ru.disdev.model.TimeTable;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
@EnableJpaRepositories
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
    public BotCommand timeTableCommand() {
        return new TimeTableCommand("/tt", "Показывает расписание");
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

    @Bean
    public TimeTable timeTable() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("time_table.json"), TimeTable.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(PaymentHandlerApplication.class, args);
	}
}
