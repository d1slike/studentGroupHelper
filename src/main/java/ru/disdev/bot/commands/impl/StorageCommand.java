package ru.disdev.bot.commands.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    protected Answer execute(CommandArgs args, long chatId, int userId) {
        if (args.size() == 0) {
            return Answer.of(mapAllFiles(fileService.getAllFiles())).withHtml();
        } else {
            String filter = args.getString("filter");
            switch (filter) {
                case "tag":
                    ImmutableCollection<DropBoxFile> filesByCategory =
                            fileService.getFilesByCategory(args.getString("name"));
                    return Answer.of(mapFileList(filesByCategory)).withHtml();
                default:
                    ImmutableList<DropBoxFile> filesByName =
                            fileService.getFilesByName(args.getString("name"));
                    return Answer.of(mapFileList(filesByName)).withHtml();
            }
        }
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
        allFiles.keys().forEach(tag -> {
            stringBuilder.append("<b>").append(tag).append(":</b>\n");
            allFiles.get(tag).forEach(dropBoxFile -> mapFileRow(stringBuilder, dropBoxFile));
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
