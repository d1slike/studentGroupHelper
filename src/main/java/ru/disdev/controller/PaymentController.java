package ru.disdev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.disdev.entity.Fio;
import ru.disdev.entity.Video;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequestMapping("/")
@PropertySource("classpath:application.properties")
public class PaymentController {

    private static final Logger LOGGER = LogManager.getLogger(PaymentController.class);

    @Value("${yandex.money.secret}")
    private String SECRET;
    private String MAIL_PATTERN;
    private Map<String, Video> goods = new HashMap<>();

    @Autowired
    private JavaMailSender sender;

    @PostConstruct
    public void init() {
        reload();
        loadMailPattern();
        LOGGER.info("Init success, loaded " + goods.size() + " goods");
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> handle(@RequestBody MultiValueMap<String, String> content) {
        Map<String, String> map = content.toSingleValueMap();
        log(map);
        if (!verify(map)) {
            Video video = goods.get(map.get("label"));
            if (video == null)
                return ResponseEntity.ok("");
            double amount = Double.parseDouble(map.get("withdraw_amount"));
            if (Math.abs(amount - video.getPrice()) < 10.) {
                Fio fio = new Fio(map.get("firstname"), map.get("lastname"), map.get("fathersname"));
                sendMail(fio, map.get("email"), video.getUrl());
            }
        }

        return ResponseEntity.ok("");
    }

    @RequestMapping(path = "reload", method = RequestMethod.GET)
    public synchronized void reload() {
        ObjectMapper mapper = new ObjectMapper();
        goods.clear();
        try {
            Stream.of(mapper.readValue(new File("goods.json"), Video[].class))
                    .forEach(video -> goods.put(video.getLabel(), video));
        } catch (IOException ignored) {

        }
        loadMailPattern();
    }

    public void sendMail(Fio fio, String targetEmail, String url) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("info@yayadance.ru");
        message.setTo(targetEmail);
        message.setSubject("Покупка видеокурса");
        message.setText(String.format(MAIL_PATTERN, fio, url));
        sender.send(message);
    }

    private void log(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        map.forEach((s, s2) -> builder.append(s).append(" = ").append(s2).append("\n"));
        builder.append("-------------------------------\n");
        LOGGER.info(builder.toString());
    }

    private void loadMailPattern() {
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get("mail.html")).forEach(builder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MAIL_PATTERN = builder.toString();
    }

    private boolean verify(Map<String, String> content) {
        boolean success = false;
        try {
            String hash = new StringBuilder()
                    .append(content.get("notification_type")).append("&")
                    .append(content.get("operation_id")).append("&")
                    .append(content.get("amount")).append("&")
                    .append(content.get("currency")).append("&")
                    .append(content.get("datetime")).append("&")
                    .append(content.get("sender")).append("&")
                    .append(content.get("codepro")).append("&")
                    .append(SECRET).append("&")
                    .append(content.get("label")).toString();
            success = content.get("sha1_hash").equals(DigestUtils.sha1Hex(hash));
        } catch (Exception ignored) {

        }
        return success;

    }
}
