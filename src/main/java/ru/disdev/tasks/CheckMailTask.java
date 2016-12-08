package ru.disdev.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.disdev.TelegramBot;
import ru.disdev.VkApi;
import ru.disdev.entity.DateTime;
import ru.disdev.entity.EmailTagLink;
import ru.disdev.repository.DateTimeRepository;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class CheckMailTask {

    private static final Logger LOGGER = Logger.getLogger(CheckMailTask.class);

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot groupBot;
    @Autowired
    private DateTimeRepository repository;

    @Value("${groupmail.login}")
    private String login;
    @Value("${groupmail.password}")
    private String password;

    private volatile Date lastCheckedDate;
    private Map<String, String> tagMap = new HashMap<>();

    @PostConstruct
    private void init() throws IOException {
        DateTime dateTime = repository.findOne(1);
        if (dateTime == null) {
            lastCheckedDate = new Date();
            repository.save(new DateTime(lastCheckedDate));
        } else {
            lastCheckedDate = dateTime.getDate();
        }
        executorService.scheduleAtFixedRate(() -> {
            Folder folder = null;
            Store store = null;
            boolean needUpdate = false;
            try {
                Session session = getSession();
                store = session.getStore("imap");
                store.connect();
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                int end = folder.getMessageCount();
                int start = Math.max(1, end - 3);
                for (Message message : folder.getMessages(start, end)) {
                    Date date = message.getReceivedDate();
                    if (date == null) {
                        continue;
                    }
                    if (date.before(lastCheckedDate) || date.equals(lastCheckedDate)) {
                        continue;
                    }

                    String notification = messageNotification(message);
                    groupBot.announceToGroup(notification);
                    //vkApi.sendMessage(notification); //todo
                    lastCheckedDate = message.getReceivedDate();
                    needUpdate = true;
                }
            } catch (MessagingException ignore) {
                LOGGER.warn(ignore.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error", e);
            } finally {
                try {
                    if (folder != null) {
                        folder.close(true);
                    }
                    if (store != null) {
                        store.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (needUpdate) {
                    repository.save(new DateTime(lastCheckedDate));
                }
            }
        }, 1, 5, TimeUnit.MINUTES);

        ObjectMapper mapper = new ObjectMapper();
        Stream.of(mapper.readValue(new File("email_links.json"), EmailTagLink[].class))
                .forEach(link -> tagMap.put(link.getEmail(), link.getTag()));
    }

    private String messageNotification(Message message) throws MessagingException {
        String from = message.getFrom()[0].toString();
        String name = "";
        String email = "";
        String tag;
        if (from.startsWith("=?")) {
            StringTokenizer tokenizer = new StringTokenizer(from, "<");
            name = tokenizer.nextToken();
            email = tokenizer.nextToken();
            email = email.substring(0, email.length() - 1).trim();
            tag = tagMap.containsKey(email) ? tagMap.get(email) : "";
            try {
                name = MimeUtility.decodeWord(name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            from = from.trim();
            tag = tagMap.containsKey(from) ? tagMap.get(from) : "";
        }

        String subject = message.getSubject();
        StringBuilder builder = new StringBuilder("Новое сообщение на почте группы:\n");
        if (!tag.isEmpty()) {
            builder.append("#").append(tag).append("@idb1409group\n");
        }

        builder.append("От: ").append(from.startsWith("=?") ? name + email : from).append("\n")
                .append("Тема: ").append(subject == null || subject.isEmpty()
                ? "Без темы" : subject).append("\n")
                .append("---");
        return builder.toString();
    }

    private Session getSession() {
        return Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(login, password);
            }
        });
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.imap.host", "imap.yandex.ru");
        properties.put("mail.imap.port", 993);
        properties.put("mail.imap.ssl.enable", true);
        //properties.put("mail.imap.starttls.enable", true);
        properties.put("mail.imap.auth", true);
        return properties;
    }
}
