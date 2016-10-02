package ru.disdev;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.Actor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.OAuthException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class VkApi {

    @Autowired
    private Properties properties;

    private TransportClient transportClient = HttpTransportClient.getInstance();
    private VkApiClient vk = new VkApiClient(transportClient);

    @PostConstruct
    public void init() {
        /*try {
            HttpClient client = HttpClientBuilder
                    .create()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            HttpGet get = new HttpGet("https://oauth.vk.com/authorize?client_id=5612054&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=wall,offline&response_type=code&v=5.57");
            String code = (String) client.execute(get).getParams().getParameter("code");
            properties.appCode = code;
            System.out.printf(code);
        } catch (IOException e) {
            //e.printStackTrace();
        }*/

    }

    public void makePost(String text) {
        Actor actor = actor();
        if (actor != null) {
            try {
                vk.wall()
                        .post(actor)
                        .fromGroup(true)
                        .message(text)
                        .ownerId(-properties.groupId)
                        .execute();
            } catch (ApiException | ClientException e) {
                e.printStackTrace();
            }
        }
    }

    private Actor actor() {
        AuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(properties.appId,
                            properties.appSecret,
                            properties.appRedirect,
                            properties.appCode)
                    .execute();
        } catch (OAuthException e) {
            e.getRedirectUri();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }

        if (authResponse != null) {
            return new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        }
        return null;
    }

}
