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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class VkApi {

    private static final Logger LOGGER = Logger.getLogger(VkApi.class);
    private static final int PEAR_ID = 2000000073;

    @Value("${vk.api.user.token}")
    private String userToken;
    @Value("${vk.api.user.id}")
    private int userId;
    @Value("${vk.api.group.token}")
    private String groupToken;
    @Value("${vk.api.group.id}")
    private int groupId;

    private TransportClient transportClient = new HttpTransportClient();
    private VkApiClient apiClient = new VkApiClient(transportClient);
    private Random random = new Random();

    public void announceAboutPost(int postId) {
        try {
            apiClient.messages()
                    .send(userActor())
                    .randomId(random.nextInt())
                    .attachment("wall" + (-groupId) + "_" + postId)
                    .peerId(PEAR_ID)
                    .execute();
        } catch (Exception e) {
            LOGGER.error("Error while sending messages", e);
        }

    }

    public void announceMessage(String message) {
        try {
            apiClient.messages()
                    .send(userActor())
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
                    .ownerId(-groupId)
                    .execute();
        } catch (ApiException | ClientException e) {
            LOGGER.error("Error while posting to group wall", e);
        }
    }

    private Actor groupActor() {
        return new GroupActor(groupId, groupToken);
    }

    private Actor userActor() {
        return new UserActor(userId, userToken);
    }
}
