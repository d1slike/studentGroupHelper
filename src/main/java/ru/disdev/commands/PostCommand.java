package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.model.flows.PostFlow;

import java.util.List;


public class PostCommand extends BotCommand {

    public PostCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Autowired
    private VkApi vkApi;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        TelegramBot bot = (TelegramBot) absSender;
        if (!botSuperusers.contains(user.getId())) {
            return;
        }

        bot.startFlow(PostFlow.class, chat.getId()).appendOnFinish(o -> {
            //vkApi.wallGroupPost(o.toString()); //TODO fix validation
            bot.announceToGroup(o.toString());
            bot.sendMessage(chat.getId(), "Успешно!", TelegramKeyBoards.defaultKeyBoard());
        });
    }
}
