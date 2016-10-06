package ru.disdev;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.disdev.entity.tokens.AccessToken;
import ru.disdev.repository.TokenRepository;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class VkApi {

    private static final Logger LOGGER = Logger.getLogger(VkApi.class);
    private static final int PEAR_ID = 2000000073;

    @Autowired
    private Properties properties;
    @Autowired
    private TokenRepository tokenRepository;

    private TransportClient transportClient = new HttpTransportClient();
    private VkApiClient apiClient = new VkApiClient(transportClient);
    private Random random = new Random();

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

    public void announceAboutPost(int postId) {
        try {
            apiClient.messages()
                    .send(groupActor())
                    .randomId(random.nextInt())
                    .attachment("wall" + (-properties.vkGroupId) + "_" + postId)
                    .peerId(PEAR_ID)
                    .execute();
        } catch (Exception e) {
            LOGGER.error("Error while sending messages", e);
        }

    }

    public void announceMessage(String message) {
        try {
            apiClient.messages()
                    .send(groupActor())
                    .randomId(random.nextInt())
                    .message(message)
                    .peerId(PEAR_ID)
                    .execute();
        } catch (Exception e) {
            LOGGER.error("Error while sending messages", e);
        }
    }

    public void makePost(String text) {
        try {
            apiClient.wall()
                    .post(userActor())
                    .message(text)
                    .fromGroup(true)
                    .ownerId(-properties.vkGroupId)
                    .execute();
        } catch (ApiException | ClientException e) {
            LOGGER.error("Error while posting to group wall", e);
        }
    }

    private Actor groupActor() {
        return new GroupActor(properties.vkGroupId, properties.vkUserAccessToken);
    }

    private Actor userActor() {
        return new UserActor(properties.vkUserId, properties.vkUserAccessToken);
    }
}
