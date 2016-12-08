package ru.disdev;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAuthValidationException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import com.vk.api.sdk.queries.wall.WallPostQuery;
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

    public void wallGroupPost(String text, String... attachments) {
        if (!checkArgs(text, attachments)) {
            return;
        }
        try {
            WallPostQuery wallPostQuery = apiClient.wall()
                    .post(userActor())
                    .fromGroup(true)
                    .ownerId(-groupId);

            if (attachments != null && attachments.length > 0) {
                wallPostQuery.attachments(attachments);
            }

            if (text != null && !text.isEmpty()) {
                wallPostQuery.message(text);
            }

            wallPostQuery.execute();
        } catch (ApiAuthValidationException ex) {
            LOGGER.error("Error while posting to wall. Validation reburied. Url: " + ex.getRedirectUri());
        } catch (Exception e) {
            LOGGER.error("Error while posting to group wall", e);
        }
    }

    public void sendMessage(String text, String... attachments) {
        if (!checkArgs(text, attachments)) {
            return;
        }
        try {
            MessagesSendQuery messagesSendQuery = apiClient.messages()
                    .send(userActor())
                    .randomId(random.nextInt())
                    .peerId(PEAR_ID);
            if (attachments != null && attachments.length > 0) {
                messagesSendQuery.attachment(attachments);
            }

            if (text != null && !text.isEmpty()) {
                messagesSendQuery.message(text);
            }

            messagesSendQuery.execute();
        } catch (ApiAuthValidationException ex) {
            LOGGER.error("Error while sending message. Validation reburied. Url: " + ex.getRedirectUri());
        } catch (Exception e) {
            LOGGER.error("Error while sending messages", e);
        }
    }

    private boolean checkArgs(String text, String... attachments) {
        return (text != null && !text.isEmpty()) || (attachments != null && attachments.length > 0);
    }

    private GroupActor groupActor() {
        return new GroupActor(groupId, groupToken);
    }

    private UserActor userActor() {
        return new UserActor(userId, userToken);
    }
}
