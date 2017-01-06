package ru.disdev.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAuthValidationException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.docs.responses.DocUploadResponse;
import com.vk.api.sdk.objects.docs.responses.GetWallUploadServerResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import com.vk.api.sdk.queries.wall.WallPostQuery;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.disdev.util.VkUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class VkApi {

    private static final Logger LOGGER = Logger.getLogger(VkApi.class);

    @Value("${vk.api.user.token}")
    private String userToken;
    @Value("${vk.api.user.id}")
    private int userId;
    @Value("${vk.api.group.token}")
    private String groupToken;
    @Value("${vk.api.group.id}")
    private int groupId;
    @Value("${vk.api.pear_id}")
    private int pearId;
    @Autowired
    private ObjectMapper mapper;
    private TransportClient transportClient = new HttpTransportClient();
    private VkApiClient apiClient = new VkApiClient(transportClient);
    private Random random = new Random();

    public boolean wallGroupPost(String text) {
        return wallGroupPost(text, null);
    }

    public boolean wallGroupPost(String text, List<String> attachments) {
        if (!checkArgs(text, attachments)) {
            return false;
        }
        try {
            WallPostQuery wallPostQuery = apiClient.wall()
                    .post(userActor())
                    .fromGroup(true)
                    .ownerId(-groupId);
            if (attachments != null && !attachments.isEmpty()) {
                wallPostQuery.attachments(attachments);
            }

            if (text != null && !text.isEmpty()) {
                wallPostQuery.message(text);
            }
            PostResponse response = wallPostQuery.execute();
            return response != null;
        } catch (ApiAuthValidationException ex) {
            LOGGER.error("Error while posting to wall. Validation required. Url: " + ex.getRedirectUri());
        } catch (Exception e) {
            LOGGER.error("Error while posting to group wall", e);
        }

        return false;
    }

    public void sendMessage(String text) {
        sendMessage(text, null);
    }

    public void sendMessage(String text, List<String> attachments) {
        if (!checkArgs(text, attachments)) {
            return;
        }
        try {
            MessagesSendQuery messagesSendQuery = apiClient.messages()
                    .send(userActor())
                    .randomId(random.nextInt())
                    .peerId(pearId);
            if (attachments != null && !attachments.isEmpty()) {
                messagesSendQuery.attachment(attachments);
            }

            if (text != null && !text.isEmpty()) {
                messagesSendQuery.message(text);
            }

            messagesSendQuery.execute();
        } catch (ApiAuthValidationException ex) {
            LOGGER.error("Error while sending message. Validation required. Url: " + ex.getRedirectUri());
        } catch (Exception e) {
            LOGGER.error("Error while sending messages", e);
        }
    }

    public List<String> uploadDocsToGroup(Map<String, File> docs) {
        List<String> result = new ArrayList<>();
        docs.forEach((fileName, file) -> {
            try {
                GetWallUploadServerResponse wallUploadServerResponse = apiClient.docs()
                        .getWallUploadServer(groupActor())
                        .groupId(groupId)
                        .execute();
                DocUploadResponse uploadResponse = apiClient.upload()
                        .doc(wallUploadServerResponse.getUploadUrl(), file)
                        .execute();
                String response = apiClient.docs()
                        .save(groupActor(), uploadResponse.getFile())
                        .title(fileName)
                        .executeAsString();
                JsonNode root = mapper.readTree(response);
                JsonNode res = root.get("response");
                JsonNode doc = res.get(0);
                if (doc != null) {
                    result.add(VkUtils.docAttachment(doc.get("owner_id").asInt(),
                            doc.get("id").asInt()));
                }
            } catch (Exception e) {
                String message = new StringBuilder("Error while uploading doc attachment ")
                        .append(fileName)
                        .append("(").append(file.getAbsolutePath()).append(") ")
                        .append("to vk")
                        .toString();
                LOGGER.error(message, e);
            }
        });
        return result;
    }

    private boolean checkArgs(String text, List<String> attachments) {
        return (text != null && !text.isEmpty()) || (attachments != null && !attachments.isEmpty());
    }

    private GroupActor groupActor() {
        return new GroupActor(groupId, groupToken);
    }

    private UserActor userActor() {
        return new UserActor(userId, userToken);
    }
}
