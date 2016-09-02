package ru.disdev;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VkApi {
    private volatile String accessToken = "fe6e3b3fc9314199d507e19027522d53e406d58246e5ba3f3597971c59c3cfba1166a0caaad85fe271b6f";
    private HttpClient httpClient = new HttpClient();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ScheduledExecutorService executorService;

    @PostConstruct
    public void init() {
        requestAccessToken();
    }

    public int post(long groupId, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("owner_id", String.valueOf(-groupId));
        params.put("from_group", "1");
        params.put("message", message);
        PostMethod method = new PostMethod(makeUrl("wall.post", params));
        execute(method);
        try {
            String response = method.getResponseBodyAsString();
            response = response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return method.getStatusCode();
    }

    private void requestAccessToken() {
        PostMethod method = new PostMethod("http://api.vkontakte.ru/oauth/authorize?client_id=5612054&scope=wall&redirect_uri=https://l2craftlife.ru:/vk/access&response_type=token");
        execute(method);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void scheduleNext(int expireTime) {
        executorService.schedule(this::requestAccessToken, expireTime / 2, TimeUnit.SECONDS);
    }

    private String makeUrl(String method, Map<String, String> params) {
        StringBuilder builder = new StringBuilder("https://api.vk.com/method/")
                .append(method)
                .append("?");
        params.forEach((s, s2) -> builder.append(s).append("=").append(s2).append("&"));
        builder.append("access_token=").append(accessToken).append("&").append("v=5.53");
        return builder.toString();
    }

    private void execute(HttpMethod method) {
        try {
            httpClient.executeMethod(method);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
