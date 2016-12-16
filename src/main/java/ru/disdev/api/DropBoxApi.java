package ru.disdev.api;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.disdev.StudentHelperApplication;

import javax.annotation.PostConstruct;

@Component
public class DropBoxApi {

    private DbxClientV2 client;

    @PostConstruct
    private void init(@Value("${dropbox.api.token}") String token) {
        DbxRequestConfig config = new DbxRequestConfig(StudentHelperApplication.class.getSimpleName());
        client = new DbxClientV2(config, token);
    }

}
