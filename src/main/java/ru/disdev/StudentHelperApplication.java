package ru.disdev;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.disdev.model.SuperusersList;
import ru.disdev.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
@EnableJpaRepositories
public class StudentHelperApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    public SuperusersList superusersList() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(IOUtils.resourceAsStream("/superusers.json"), new TypeReference<List<Integer>>() {
        });
        return new SuperusersList(ImmutableList.copyOf(list));
    }

    @Bean
    public JavaMailSender mailSender(@Value("${mail.server}") String mailServer,
                                     @Value("${mail.port}") int mailPort,
                                     @Value("${mail.user}") String mailUser,
                                     @Value("${mail.password}") String mailPassword,
                                     @Value("${mail.protocol}") String mailProtocol,
                                     @Value("${mail.enable.tsl}") boolean mailEnableTsl) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailServer);
        javaMailSender.setPort(mailPort);
        javaMailSender.setUsername(mailUser);
        javaMailSender.setPassword(mailPassword);
        javaMailSender.setDefaultEncoding("UTF-8");
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", mailProtocol);
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", mailEnableTsl);
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(StudentHelperApplication.class, args);
    }
}
