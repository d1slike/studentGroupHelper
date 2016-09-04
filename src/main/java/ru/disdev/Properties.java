package ru.disdev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Dislike on 04.09.2016.
 */
@Component
public class Properties {
    @Value("${mail.server}")
    public String mailServer;
    @Value("${mail.port}")
    public int mailPort;
    @Value("${mail.user}")
    public String mailUser;
    @Value("${mail.password}")
    public String mailPassword;
    @Value("${mail.protocol}")
    public String mailProtocol;
    @Value("${mail.enable.tsl}")
    public boolean mailEnableTsl;
    @Value("${telegram.bot.token}")
    public String botToken;
    @Value("${telegram.bot.name}")
    public String botName;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;
    @Value("${telegram.bot.secret.hello}")
    public String botSecretHello;
    @Value("${telegram.bot.secret.bye}")
    public String botSecretBye;
    @Value("${yandex.money.secret}")
    public String yandexMoneySecret;
    @Value("${vk.access_token}")
    public String vkAccessToken;
}
