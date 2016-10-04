package ru.disdev;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.disdev.entity.tokens.AccessToken;
import ru.disdev.repository.TokenRepository;

import javax.annotation.PostConstruct;

@Component
public class VkApi {

    private static final Logger LOGGER = Logger.getLogger(VkApi.class);

    @Autowired
    private Properties properties;
    @Autowired
    private TokenRepository tokenRepository;


    @PostConstruct
    private void init() {
        AccessToken userToken = tokenRepository.getUserToken();
        AccessToken groupToken = tokenRepository.getGroupToken();

        if (userToken != null) {
            properties.vkUserAccessToken = userToken.getToken();
            properties.vkUserId = userToken.getId();
        }

        if (groupToken != null) {
            properties.vkGroupAccessToken = groupToken.getToken();
        }

        LOGGER.info(String.format("User token: %s, Group token: %s", properties.vkUserAccessToken, properties.vkGroupAccessToken));
    }




}
