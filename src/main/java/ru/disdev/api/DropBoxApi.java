package ru.disdev.api;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.FileLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.disdev.StudentHelperApplication;
import ru.disdev.entity.DropBoxFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DropBoxApi {

    private DbxClientV2 client;
    @Value("${dropbox.api.token}")
    private String token;

    @PostConstruct
    private void init() {
        DbxRequestConfig config = new DbxRequestConfig(StudentHelperApplication.class.getSimpleName());
        client = new DbxClientV2(config, token);
    }

    public Map<String, String> getFullSharingData() throws DbxException {
        return client.sharing().listSharedLinks().getLinks().stream()
                .filter(link -> link instanceof FileLinkMetadata)
                .collect(Collectors.toMap(SharedLinkMetadata::getName, SharedLinkMetadata::getUrl));
    }

    public Multimap<String, FileMetadata> getFullFileData() throws DbxException {
        Multimap<String, FileMetadata> data = ArrayListMultimap.create();
        findAllFilesInDir(data, "");
        return data;
    }

    private void findAllFilesInDir(Multimap<String, FileMetadata> map, String folder) throws DbxException {
        for (Metadata metadata : client.files().listFolder(folder).getEntries()) {
            if (metadata instanceof FileMetadata) {
                map.put(folder, (FileMetadata) metadata);
            } else if (metadata instanceof FolderMetadata) {
                findAllFilesInDir(map, folder + "/" + metadata.getName());
            }
        }
    }

    public DropBoxFile uploadFile(File file, String path) throws DbxException, IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            FileMetadata fileMetadata = client.files().upload("/" + path).uploadAndFinish(inputStream);
            SharedLinkMetadata linkMetadata =
                    client.sharing().createSharedLinkWithSettings(fileMetadata.getPathDisplay());
            return new DropBoxFile(fileMetadata.getName(), linkMetadata.getUrl(), fileMetadata.getServerModified());
        }
    }
}
