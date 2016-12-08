package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.Properties;
import ru.disdev.TelegramBot;
import ru.disdev.VkApi;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.model.flows.PostFlow;


public class PostCommand extends BotCommand {

    public PostCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Autowired
    private Properties properties;
    @Autowired
    private VkApi vkApi;

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        TelegramBot bot = (TelegramBot) absSender;
        if (!properties.botSuperusers.contains(user.getId())) {
            return;
        }

        bot.startFlow(PostFlow.class, chat.getId()).appendOnFinish(o -> {
            //vkApi.wallGroupPost(o.toString()); //TODO fix validation
            bot.announceToGroup(o.toString());
            bot.sendMessage(chat.getId(), "Успешно!", TelegramKeyBoards.defaultKeyBoard());
        });
    }
}
