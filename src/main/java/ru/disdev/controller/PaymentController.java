package ru.disdev.controller;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.disdev.entity.Client;
import ru.disdev.entity.Fio;
import ru.disdev.entity.Video;
import ru.disdev.repository.ClientsRepository;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.disdev.util.FileUtils.loadFileContentAsString;

@Controller
@RequestMapping("/")
public class PaymentController {

    private static final Logger LOGGER = LogManager.getLogger(PaymentController.class);
    private static final String GET_VIDEO_URL = "https://l2craftlife.ru/video?email=<email>&accessToken=<token>&label=<label>";
    private static final String SEND_CORRECT_GMAIL = "https://l2craftlife.ru/gmail?accessToken=<token>";
    private static final String[] FILE_HEADER = {"Имя", "Контактная почта", "Аккаунт YouTube", "Дата покупки", "Окончание доступа"};

    @Value("${yandex.money.secret}")
    public String yandexMoneySecret;
    @Value("${api.token}")
    public String apiToken;
    @Value("${mail.admin}")
    public String adminEmail;

    @Autowired
    private JavaMailSender sender;
    @Autowired
    private ClientsRepository clientsRepository;
    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private ObjectMapper mapper;

    private String successPaymentMail;
    private String badGmailMail;
    private String finalMail;
    private String newClientMail;
    private String removeClientMail;
    private Map<String, Video> goods = new HashMap<>();

    @PostConstruct
    public void init() {
        reload(apiToken);
        executorService.scheduleAtFixedRate(() -> {
            Date now = new Date();
            StringBuilder builder = new StringBuilder("");
            clientsRepository
                    .findByStatus(Client.VALID)
                    .stream()
                    .filter(client -> client.getExpireDate().before(now))
                    .collect(Collectors.toMap(Client::getId, Client::getGmail))
                    .forEach((id, gmail) -> {
                        builder.append(gmail);
                        clientsRepository.delete(id);
                    });
            String list = builder.toString();
            if (!list.isEmpty()) {
                sendMail(adminEmail, removeClientMail.replace("%list%", list));
            }
        }, 3, 3, TimeUnit.HOURS);

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

                Client client = new Client();
                client.setAccessToken(token);
                client.setName(fio.toString());
                client.setContactEmail(email);
                client.setProductId(productId);
                client.setPaymentDate(new Date());
                client.setStatus(Client.NEW_CLIENT);
                boolean correctGMail = StringUtils.countOccurrencesOf(email, "gmail.com") > 0;
                if (correctGMail) {
                    client.setGmail(email);
                }
                clientsRepository.save(client);

                String url;
                String text;
                if (correctGMail) {
                    url = GET_VIDEO_URL
                            .replace("<email>", email)
                            .replace("<token>", token)
                            .replace("<label>", productId);
                    text = successPaymentMail;
                } else {
                    url = SEND_CORRECT_GMAIL.replace("<token>", token);
                    text = badGmailMail;
                }

                text = text.replace("%url%", url);
                sendMail(email, text);
            }
        }
        return ResponseEntity.ok("");
    }

    @RequestMapping(path = "/gmail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String setGMail(@RequestParam String accessToken, @RequestBody MultiValueMap<String, String> body) {
        Client client = clientsRepository.findOneByAccessTokenAndStatus(accessToken, Client.NEW_CLIENT);
        if (client != null && (client.getGmail() == null || client.getGmail().isEmpty())) {
            Map<String, String> map = body.toSingleValueMap();
            if (body.containsKey("email")) {
                String email = map.get("email");
                if (StringUtils.countOccurrencesOf(email, "gmail.com") > 0) {

                    client.setGmail(email);
                    clientsRepository.save(client);

                    String url = GET_VIDEO_URL
                            .replace("<email>", email)
                            .replace("<token>", client.getAccessToken())
                            .replace("<label>", client.getProductId());
                    String text = successPaymentMail.replace("%url%", url);
                    sendMail(client.getContactEmail(), text);
                    return "redirect:http://yayadance.ru"; //todo заменит на страничку
                }
            }
        }
        return "redirect:" + "http://yayadance.ru/index.php/component/content/article?id=33"; //todo сменить
    }


    @RequestMapping(path = "/video", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String getVideo(@RequestParam String email,
                           @RequestParam String accessToken,
                           @RequestParam String label,
                           @RequestBody MultiValueMap<String, String> body) {

        Client client = clientsRepository.findOneByAccessTokenAndStatus(accessToken, Client.NEW_CLIENT);
        if (client != null && client.getGmail() != null && !client.getGmail().isEmpty()) {
            Map<String, String> map = body.toSingleValueMap();
            String first = map.get("first");
            String second = map.get("second");
            if (first != null && second != null) {
                boolean acceptFirst = Boolean.parseBoolean(first);
                boolean acceptSecond = Boolean.parseBoolean(second);
                if (acceptFirst && acceptSecond) {
                    long expireTime = System.currentTimeMillis();
                    expireTime += 30 * 24 * 60 * 60 * 1000L;
                    client.setExpireDate(new Date(expireTime));
                    client.setStatus(Client.VALID);
                    clientsRepository.save(client);
                    String text = newClientMail.replace("%name%", client.getName())
                            .replace("%email%", client.getGmail())
                            .replace("%date%", Client.DATE_FORMAT.format(client.getExpireDate()));
                    sendMail(adminEmail, text);
                    StringTokenizer tokenizer = new StringTokenizer(label, "_");
                    String videoKey = tokenizer.nextToken();
                    String lang = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "ru";
                    if (goods.containsKey(videoKey)) {
                        sendMail(client.getContactEmail(), finalMail.replace("%video%", goods.get(videoKey).getUrl()));
                    }
                }
            }
        }
        return "redirect:" + "http://yayadance.ru/";
    }

    @RequestMapping(path = "/reload", method = RequestMethod.GET)
    public synchronized ResponseEntity<String> reload(@RequestParam String token) {
        if (!token.equals(apiToken)) {
            return ResponseEntity.ok("Bad token");
        }
        goods.clear();
        try {
            Stream.of(mapper.readValue(new File("goods.json"), Video[].class))
                    .forEach(video -> goods.put(video.getLabel(), video));
        } catch (IOException ignored) {

        }
        successPaymentMail = loadFileContentAsString("success.html");
        badGmailMail = loadFileContentAsString("bad_gmail.html");
        finalMail = loadFileContentAsString("final.html");
        removeClientMail = loadFileContentAsString("remove_client.html");
        newClientMail = loadFileContentAsString("new_client.html");

        return ResponseEntity.ok("OK");
    }

    @RequestMapping(path = "/file")
    public void getFile(@RequestParam String token, HttpServletResponse response) {
        if (!token.equals(apiToken)) {
            return;
        }
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/csv");
        response.setCharacterEncoding("Windows-1251");
        response.addHeader("Content-Disposition", "attachment;filename=users.csv");

        try (CSVWriter writer = new CSVWriter(response.getWriter(), ';')) {
            writer.writeNext(FILE_HEADER);
            clientsRepository
                    .findAll()
                    .forEach(client -> writer.writeNext(client.toCSVRow()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMail(String targetEmail, String text) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, false);
            helper.setFrom("info@yayadance.ru");
            helper.setTo(targetEmail);
            helper.setSubject("Покупка видеокурса");
            helper.setText(text, true);
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
                    .append(yandexMoneySecret).append("&")
                    .append(content.get("label")).toString();
            success = content.get("sha1_hash").equals(DigestUtils.sha1Hex(hash));
        } catch (Exception ignored) {

        }
        return success;

    }
}
