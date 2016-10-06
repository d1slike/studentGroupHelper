package ru.disdev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Properties {
    @Value("${api.token}")
    public String apiToken;

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
    @Value("${mail.admin}")
    public String adminEmail;

    @Value("${telegram.bot.token}")
    public String botToken;
    @Value("${telegram.bot.name}")
    public String botName;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;

    @Value("${yandex.money.secret}")
    public String yandexMoneySecret;


    @Value("${vk.group_id}")
    public int vkGroupId;

    public int vkUserId;
    public String vkUserAccessToken;
    public String vkGroupAccessToken;


}
