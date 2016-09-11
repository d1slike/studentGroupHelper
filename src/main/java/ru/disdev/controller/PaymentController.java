package ru.disdev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.disdev.Properties;
import ru.disdev.entity.AccessToken;
import ru.disdev.entity.Fio;
import ru.disdev.entity.Video;
import ru.disdev.repository.TokenRepository;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Controller
@RequestMapping("/")
public class PaymentController {

    private static final Logger LOGGER = LogManager.getLogger(PaymentController.class);

    private String informationMail;
    private Map<String, Video> goods = new HashMap<>();

    @Autowired
    private JavaMailSender sender;
    @Autowired
    private Properties properties;
    @Autowired
    private TokenRepository repository;

    @PostConstruct
    public void init() {
        reload();
        LOGGER.info("Init success, loaded " + goods.size() + " goods");
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> handle(@RequestBody MultiValueMap<String, String> content) {
        Map<String, String> map = content.toSingleValueMap();
        log(map);
        if (verify(map)) {
            Video video = goods.get(map.get("label"));
            if (video == null)
                return ResponseEntity.ok("");
            double amount = Double.parseDouble(map.get("withdraw_amount"));
            if (Math.abs(amount - video.getPrice()) < 10.) {
                Fio fio = new Fio(map.get("firstname"), map.get("lastname"), map.get("fathersname"));
                String email = map.get("email");
                String token = UUID.randomUUID().toString();
                String productId = video.getLabel();
                repository.save(new AccessToken(email, token, productId));
                String url = "https://l2craftlife.ru/?email=" + email + "&accessToken=" + token + "&label=" + productId;
                sendInformationMail(fio, email, url);
                //sendInformationMail(fio, map.get("email"), video.getUrl());
            }
        }

        return ResponseEntity.ok("");
    }


    @RequestMapping(method = RequestMethod.GET)
    public String getVideo(@RequestParam String email, @RequestParam String accessToken, @RequestParam String label) {
        AccessToken token = repository.findOneByEmail(email);
        if (token != null && token.getToken().equals(accessToken) && token.getProductId().equals(label)) {
            String url = goods.get(label).getUrl();
            sendPlaylistUrl(email, url);
            repository.delete(token);
            return "redirect:" + url;
        }

        return "redirect:" + "http://yayadance.ru/";
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

    public void sendInformationMail(Fio fio, String targetEmail, String url) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, false);
            helper.setFrom("info@yayadance.ru");
            helper.setTo(targetEmail);
            helper.setSubject("Покупка видеокурса");
            helper.setText(String.format(informationMail, url), true);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendPlaylistUrl(String email, String url) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, false);
            helper.setFrom("info@yayadance.ru");
            helper.setTo(email);
            helper.setSubject("Покупка видеокурса");
            helper.setText("Ваша ссылка для просмотра видео: " + url);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
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
        informationMail = builder.toString();
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
                    .append(properties.yandexMoneySecret).append("&")
                    .append(content.get("label")).toString();
            success = content.get("sha1_hash").equals(DigestUtils.sha1Hex(hash));
        } catch (Exception ignored) {

        }
        return success;

    }
}
