package ru.disdev.bot.commands.impl;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.mail.Teacher;
import ru.disdev.model.Answer;
import ru.disdev.service.TeacherService;

@Request(command = "/teach")
public class TeacherCommand extends AbstractRequest {

    @Autowired
    private TeacherService teacherService;


    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
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
        return Answer.of(builder.toString()).withHtml();
    }
}
