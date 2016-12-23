package ru.disdev.bot.commands.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.Answer;
import ru.disdev.entity.DropBoxFile;
import ru.disdev.service.FileService;

@Request(command = "/file", args = {"filter", "name"})
public class StorageCommand extends AbstractRequest {

    private static final String NO_FILES = "<b>Нет файлов</b>";

    @Autowired
    private FileService fileService;
    @Autowired
    private TelegramBot bot;

    @Override
    protected Answer execute(CommandArgs args, long chatId, int userId) {
        String answer;
        if (args.size() == 0) {
            answer = mapAllFiles(fileService.getAllFiles());
        } else {
            String filter = args.getString("filter");
            switch (filter) {
                case "tag":
                    ImmutableCollection<DropBoxFile> filesByCategory =
                            fileService.getFilesByCategory(args.getString("name"));
                    answer = mapFileList(filesByCategory);
                    break;
                default:
                    ImmutableList<DropBoxFile> filesByName =
                            fileService.getFilesByName(args.getString("name"));
                    answer = mapFileList(filesByName);
            }
        }
        SendMessage message = new SendMessage()
                .setText(answer)
                .enableHtml(true)
                .enableNotification()
                .disableWebPagePreview()
                .setChatId(chatId);
        bot.sendFormattedMessage(message);
        return Answer.empty();
    }

    private String mapFileList(ImmutableCollection<DropBoxFile> files) {
        if (files.isEmpty()) {
            return NO_FILES;
        }
        StringBuilder builder = new StringBuilder();
        files.forEach(dropBoxFile -> mapFileRow(builder, dropBoxFile));
        return builder.toString();
    }

    private String mapAllFiles(ImmutableMultimap<String, DropBoxFile> allFiles) {
        if (allFiles.isEmpty()) {
            return NO_FILES;
        }
        StringBuilder stringBuilder = new StringBuilder();
        allFiles.asMap().forEach((tag, files) -> {
            stringBuilder.append("<b>")
                    .append(Character.toUpperCase(tag.charAt(0)))
                    .append(tag.substring(1))
                    .append(":</b>\n");
            files.forEach(dropBoxFile -> mapFileRow(stringBuilder, dropBoxFile));
            stringBuilder.append("+++++++++++++++++++++++++++\n");
        });
        return stringBuilder.toString();
    }

    private void mapFileRow(StringBuilder builder, DropBoxFile dropBoxFile) {
        builder.append("<a href=\"")
                .append(dropBoxFile.getPublicLink())
                .append("\">")
                .append(dropBoxFile.getName())
                .append("</a>\n");
    }
}
