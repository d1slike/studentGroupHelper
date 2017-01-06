package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.Post;
import ru.disdev.entity.TelegramAttachment;
import ru.disdev.model.Answer;
import ru.disdev.model.flows.PostFlow;
import ru.disdev.service.OptionsService;
import ru.disdev.service.RemoteResourceService;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Request(command = "/post")
public class PostCommand extends AbstractRequest {

    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot bot;
    @Autowired
    private OptionsService optionsService;
    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private RemoteResourceService remoteResourceService;

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (!optionsService.isSuperUser(userId)) {
            return Answer.of("Нет прав");
        }
        bot.startFlow(PostFlow.class, chatId)
                .appendOnFinish(post -> handlePost(post, chatId));

        return Answer.empty();
    }

    public void handlePost(Post post, long chatId) {
        List<TelegramAttachment> attachments = post.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            bot.sendMessage(chatId, "Подготовка вложений...");
            executorService.execute(() -> {
                List<String> vkAttachments = remoteResourceService.prepareVkAttachments(post);
                boolean success = vkApi.wallGroupPost(post.toString(), vkAttachments);
                String message = new StringBuilder(success ? "Успешно!" : "Не удалось опубликовать.")
                        .append("\n")
                        .append("Получено вложений: ").append(attachments.size()).append("\n")
                        .append("Опубликовано вложений: ").append(vkAttachments.size()).append("\n")
                        .toString();
                bot.sendMessage(chatId, message);
            });
        } else {
            boolean success = vkApi.wallGroupPost(post.toString());
            bot.sendMessage(chatId, success ? "Успешно!" : "Не удалось опубликовать");
        }
    }
}
