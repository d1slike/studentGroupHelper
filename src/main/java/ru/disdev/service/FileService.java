package ru.disdev.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.common.collect.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.api.DropBoxApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.DropBoxFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FileService {

    public static final String UNDEFINED_CATEGORY = "Неопредлено";
    private static final String TEMP_DIR = "temp/";
    private static final Comparator<DropBoxFile> FILE_COMPARATOR =
            (a, b) -> a.getUpdateDate().compareTo(b.getUpdateDate());
    private static final Logger LOGGER = Logger.getLogger(FileService.class);

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private DropBoxApi dropBoxApi;
    private volatile ImmutableMultimap<String, DropBoxFile> cache = ImmutableMultimap.of();
    private boolean onFailSendMessage = true;

    @PostConstruct
    private void init() {
        executorService.execute(this::loadDataFromDropBox);
    }

    public void collectVkAttachments(Map<String, String> attachments, String tag) {
        executorService.execute(() -> {
            List<DropBoxFile> uploadedFiles = downloadFilesToTempDir(attachments)
                    .stream()
                    .map(file -> {
                        try {
                            return dropBoxApi.uploadFile(file, tag + "/" + file.getName());
                        } catch (DbxException | IOException e) {
                            LOGGER.error("Error while uploading file to dropbox", e);
                        }
                        return null;
                    })
                    .filter(dropBoxFile -> dropBoxFile != null)
                    .collect(Collectors.toList());
            if (!uploadedFiles.isEmpty()) {
                ImmutableCollection<DropBoxFile> categoryFiles = cache.get(tag);
                uploadedFiles.addAll(categoryFiles);
                ImmutableList<DropBoxFile> sortedCopy
                        = Ordering.from(FILE_COMPARATOR).immutableSortedCopy(uploadedFiles);
                cache = ImmutableMultimap.<String, DropBoxFile>builder().putAll(cache).putAll(tag, sortedCopy).build();
            }
            telegramBot.sendToMaster(String.format("Найдено %d вложений, загружено %d вложений\n",
                    attachments.size(), uploadedFiles.size()));
        });
    }

    public ImmutableCollection<DropBoxFile> getFilesByCategory(String tag) {
        return cache.get(tag);
    }

    public List<DropBoxFile> getFilesByName(String name) {
        return cache.values().stream()
                .filter(dropBoxFile -> dropBoxFile.getName().contains(name))
                .collect(Collectors.toList());
    }

    private List<File> downloadFilesToTempDir(Map<String, String> attachments) {
        List<File> files = new ArrayList<>();
        attachments.forEach((url, name) -> {
            try {
                if (name == null || name.isEmpty()) {
                    name = url.substring(url.lastIndexOf("/"));
                }
                URL netUrl = new URL(url);
                File file = new File(TEMP_DIR + name);
                try (InputStream inputStream = netUrl.openStream();
                     OutputStream outputStream = new FileOutputStream(file)) {
                    IOUtils.copy(inputStream, outputStream);
                }
                files.add(file);
            } catch (IOException e) {
                LOGGER.error("Error while download attachment", e);
            }
        });
        return files;
    }

    private void loadDataFromDropBox() {
        try {
            Map<String, String> fullSharingData = dropBoxApi.getFullSharingData();
            Multimap<String, FileMetadata> fullFileData = dropBoxApi.getFullFileData();
            ImmutableMultimap.Builder<String, DropBoxFile> builder = ImmutableMultimap.builder();
            fullFileData.entries().forEach(entry -> {
                String fileName = entry.getValue().getName();
                Date date = entry.getValue().getServerModified();
                String url = fullSharingData.getOrDefault(fileName, "");
                builder.put(entry.getKey(), new DropBoxFile(fileName, url, date));
            });
            cache = builder.orderValuesBy(FILE_COMPARATOR).build();
            executorService.schedule(FileService.this::loadDataFromDropBox, 3, TimeUnit.HOURS);
            onFailSendMessage = true; //TODO выпилить
        } catch (DbxException e) {
            LOGGER.error("Error while loading files from dropbox", e);
            executorService.schedule(FileService.this::loadDataFromDropBox, 1, TimeUnit.HOURS);
            if (onFailSendMessage) {
                telegramBot.sendToMaster("Ошибка при загрузке данных с dropbox!");
            }
        }
    }
}
