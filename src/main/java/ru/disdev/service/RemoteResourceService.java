package ru.disdev.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.GetFile;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.post.Post;
import ru.disdev.entity.post.TelegramAttachment;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RemoteResourceService {

    private static final Logger LOGGER = Logger.getLogger(RemoteResourceService.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private VkApi vkApi;

    public List<File> downloadFilesFromVkToTempDir(Map<String, String> attachments, String directory) {
        List<File> files = new ArrayList<>();
        attachments.forEach((url, name) -> {
            try {
                if (name == null || name.isEmpty()) {
                    name = url.substring(url.lastIndexOf("/") + 1);
                }
                File file = new File(directory + name);
                FileUtils.copyURLToFile(new URL(url), file);
                files.add(file);
            } catch (Exception e) {
                LOGGER.error("Error while download attachment from vk", e);
            }
        });
        return files;
    }

    private Map<String, File> downloadFilesFromTelegram(List<TelegramAttachment> attachments) {
        return attachments.stream()
                .map(attachment -> {
                    try {
                        org.telegram.telegrambots.api.objects.File telegramBotFile =
                                telegramBot.getFile(new GetFile().setFileId(attachment.getId()));
                        File file = telegramBot.downloadFile(telegramBotFile);
                        file = ru.disdev.util.FileUtils.changeFileExtension(file.getAbsolutePath(), attachment.getFileExtension());
                        if (!file.exists()) {
                            return null;
                        }
                        String name = attachment.getName() + attachment.getFileExtension();
                        return Pair.of(name, file);
                    } catch (Exception e) {
                        LOGGER.error("Error while download attachment from telegram", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    public List<String> prepareVkAttachments(Post post) {
        List<TelegramAttachment> attachments = post.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, File> docs = downloadFilesFromTelegram(attachments);
        if (!docs.isEmpty()) {
            try {
                return vkApi.uploadDocsToGroup(docs);
            } finally {
                docs.forEach((s, file) -> file.delete());
            }
        }
        return new ArrayList<>();
    }
}
