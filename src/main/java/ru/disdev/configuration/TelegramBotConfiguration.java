package ru.disdev.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.commands.EventCommand;
import ru.disdev.commands.PostCommand;
import ru.disdev.commands.StartCommand;
import ru.disdev.commands.TimeTableCommand;

@Configuration
public class TelegramBotConfiguration {

    @Bean
    public BotCommand timeTableCommand() {
        return new TimeTableCommand("/tt", "Показывает расписание");
    }

    @Bean
    public BotCommand eventCommand() {
        return new EventCommand("/event", "Изменение событий");
    }

    @Bean
    public BotCommand postCommand() {
        return new PostCommand("/post", "Публикация нового поста в группе VK");
    }

    @Bean
    public BotCommand startCommand() {
        return new StartCommand("/start", "Инициализация основного меню бота");
    }
}
