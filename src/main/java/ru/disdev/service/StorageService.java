package ru.disdev.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.api.DropBoxApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.DropBoxFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class StorageService {

    public static final String UNDEFINED_TAG = "Неопределено";
    public static final String VK_TEMP_DIR = "./vk_temp/";
    public static final String MAIL_TEMP_DIR = "./mail_temp/";
    private static final Comparator<DropBoxFile> FILE_COMPARATOR =
            Comparator.comparing(DropBoxFile::getUpdateDate).reversed();
    private static final Logger LOGGER = Logger.getLogger(StorageService.class);
    private static final Object MONITOR = new Object();

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private DropBoxApi dropBoxApi;
    @Autowired
    private RemoteResourceService remoteResourceService;
    private volatile ImmutableMultimap<String, DropBoxFile> cache = ImmutableMultimap.of();
    private boolean onFailSendMessage = true;

    @PostConstruct
    private void init() throws IOException {
        File vkTempDir = new File(VK_TEMP_DIR);
        if (!vkTempDir.exists()) {
            vkTempDir.mkdir();
        }
        File mailTempDir = new File(MAIL_TEMP_DIR);
        if (!mailTempDir.exists()) {
            mailTempDir.mkdir();
        }
        executorService.execute(this::loadDataFromDropBox);
    }

    public void collectVkAttachments(Map<String, String> attachments, String tag) {
        executorService.execute(() -> {
            List<File> localFiles = remoteResourceService.downloadFilesFromVkToTempDir(attachments, VK_TEMP_DIR);
            batchUploadToDropBox(localFiles, tag);
        });
    }

    public void collectMailAttachments(List<File> localFiles, String tag) {
        executorService.execute(() -> batchUploadToDropBox(localFiles, tag));
    }

    public ImmutableCollection<DropBoxFile> getFilesByCategory(String tag) {
        return cache.get(tag);
    }

    public ImmutableList<DropBoxFile> getFilesByName(String name) {
        ImmutableList.Builder<DropBoxFile> builder = ImmutableList.builder();
        final String filterName = name.toLowerCase().trim();
        cache.values().stream()
                .filter(dropBoxFile -> dropBoxFile.getName().toLowerCase().contains(filterName))
                .forEach(builder::add);
        return builder.build();
    }

    public ImmutableMultimap<String, DropBoxFile> getAllFiles() {
        return cache;
    }

    private void batchUploadToDropBox(List<File> localFiles, String tag) {
        synchronized (MONITOR) {
            try {
                List<DropBoxFile> uploadedFiles = localFiles
                        .stream()
                        .map(file -> {
                            try {
                                return dropBoxApi.uploadFile(file, tag + "/" + file.getName());
                            } catch (DbxException | IOException e) {
                                LOGGER.error("Error while uploading file to dropbox", e);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                updateCache(uploadedFiles, tag);
            } finally {
                localFiles.forEach(File::delete);
            }
        }
    }

    private void updateCache(List<DropBoxFile> newFiles, String tag) {
        if (!newFiles.isEmpty()) {
            cache = ImmutableMultimap.<String, DropBoxFile>builder()
                    .putAll(cache)
                    .putAll(tag.toLowerCase(), newFiles)
                    .orderValuesBy(FILE_COMPARATOR)
                    .build();

        }
    }

    private void loadDataFromDropBox() {
        synchronized (MONITOR) {
            try {
                Map<String, String> fullSharingData = dropBoxApi.getFullSharingData();
                Multimap<String, FileMetadata> fullFileData = dropBoxApi.getFullFileData();
                ImmutableMultimap.Builder<String, DropBoxFile> builder = ImmutableMultimap.builder();
                fullFileData.entries().stream()
                        .filter(entry -> !entry.getKey().isEmpty())
                        .forEach(entry -> {
                            String fileName = entry.getValue().getName();
                            Date date = entry.getValue().getServerModified();
                            String url = fullSharingData.getOrDefault(fileName, "");
                            builder.put(entry.getKey().toLowerCase(), new DropBoxFile(fileName, url, date));
                        });
                cache = builder.orderValuesBy(FILE_COMPARATOR).build();
                executorService.schedule(this::loadDataFromDropBox, 3, TimeUnit.HOURS);
                onFailSendMessage = true; //TODO выпилить
            } catch (DbxException e) {
                LOGGER.error("Error while loading files from dropbox", e);
                executorService.schedule(this::loadDataFromDropBox, 1, TimeUnit.HOURS);
                if (onFailSendMessage) {
                    telegramBot.sendToMaster("Ошибка при загрузке данных с dropbox!");
                }
            }
        }
    }
}
