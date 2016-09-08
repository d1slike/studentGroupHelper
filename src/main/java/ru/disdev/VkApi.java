package ru.disdev;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class VkApi {
    private HttpClient httpClient = new HttpClient();

    @Autowired
    private Properties properties;

    public int sendPost(long groupId, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("owner_id", String.valueOf(-groupId));
        params.put("from_group", "1");
        try {
            message = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        params.put("message", message);
        PostMethod method = new PostMethod(makeUrl("wall.post", params));
        execute(method);
        return method.getStatusCode();
    }


    private String makeUrl(String method, Map<String, String> params) {
        StringBuilder builder = new StringBuilder("https://api.vk.com/method/")
                .append(method)
                .append("?");
        params.forEach((s, s2) -> builder.append(s).append("=").append(s2).append("&"));
        builder.append("access_token=").append(properties.vkAccessToken).append("&").append("v=5.53");
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
