package ru.disdev.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.WatchRequest;
import ru.disdev.controller.GMailController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

//@Component
public class GMailClientService {

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private HttpTransport HTTP_TRANSPORT;
    private FileDataStoreFactory DATA_STORE_FACTORY;
    private final File DATA_STORE_DIR = new File("/");
    private Gmail gmail;

    @PostConstruct
    private void init() {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            gmail = getGmailService();
            WatchRequest watchRequest = new WatchRequest();
            watchRequest
                    .set("labelIds", Collections.singletonList("INBOX"))
                    .set("topicName", "projects/chrome-weft-145509/topics/gmail");
            gmail.users().watch("me", watchRequest).execute();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

    }


    private Credential authorize() throws IOException {
        InputStream in = GMailController.class.getResourceAsStream("/client_secret_651689405604-hopm6n2v4bf26lda8dgm62btt5utci2f.apps.googleusercontent.com.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Arrays.asList(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_MODIFY, GmailScopes.GMAIL_MODIFY))
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        return new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("merser1q");
    }

    private Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("GMailReceiver")
                .build();
    }

    public Gmail get() {
        return gmail;
    }
}
