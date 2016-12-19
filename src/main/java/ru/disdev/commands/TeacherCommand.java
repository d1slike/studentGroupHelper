package ru.disdev.commands;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.mail.Teacher;
import ru.disdev.service.TeacherService;

public class TeacherCommand extends BotCommand {

    @Autowired
    private TeacherService teacherService;

    public TeacherCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        StringBuilder builder = new StringBuilder();
        ImmutableList<Teacher> teachers = teacherService.getTeachers();
        if (teachers.isEmpty()) {
            builder.append("<b>Нет данных</b>");
        } else {
            teachers.forEach(teacher -> {
                builder.append("<b>").append(teacher.getFio()).append("</b>\n")
                        .append("<i>Почта: </i>").append(teacher.getEmail()).append("\n")
                        .append("<i>Предмет: </i>").append(teacher.getTag())
                        .append("\n++++++++++++++++++++++++++++\n");
            });
        }
        TelegramBot telegramBot = (TelegramBot) absSender;
        telegramBot.sendMessage(chat.getId(), builder.toString(), true);
    }
}
