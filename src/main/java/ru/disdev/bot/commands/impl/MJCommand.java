package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.mj.MJApiAnswer;
import ru.disdev.entity.mj.MJUser;
import ru.disdev.entity.mj.Module;
import ru.disdev.entity.mj.Semesters;
import ru.disdev.model.Answer;
import ru.disdev.service.MJService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Request(command = "/mj", args = {"action", "login", "pass"})
public class MJCommand extends AbstractRequest {

    private static final String M1 = "М1";
    private static final String M2 = "М2";
    private static final String C_WORK = "K";
    private static final String CREDIT = "З";
    private static final String EXAM = "Э";
    private static final String EMPTY = "X";
    private static final String TABLE_HEADER = String.join("  ",
            Arrays.asList(M1, M2, C_WORK, CREDIT, EXAM));

    @Autowired
    private MJService mjService;

    @Override
    protected Answer execute(CommandArgs args, long chatId, int userId) {
        String action = args.getString("action");
        switch (action) {
            case "get":
                MJUser user = mjService.findByUserId(userId);
                if (user == null) {
                    return Answer.of("Аккаунт не найден. Для начала введите данные.");
                }
                MJApiAnswer response = mjService.testApi(user);
                if (response != MJApiAnswer.OK) {
                    return Answer.of(response.toString());
                }
                Semesters semesters = mjService.getSemesters(user);
                if (semesters == null) {
                    return Answer.of(MJApiAnswer.API_ERROR.toString());
                }
                List<String> list = semesters.getSemesters();
                if (list.isEmpty()) {
                    return Answer.of("Нет данных по семестрам");
                }
                String lastSemester = Collections.max(list, String::compareToIgnoreCase);
                List<Module> modules = mjService.getModules(user, lastSemester);
                return Answer.of(mapModules(modules)).withHtml();
            case "new":
                String login = args.getString("login");
                String pass = args.getString("pass");
                if (login == null || pass == null || login.isEmpty() || pass.isEmpty()) {
                    return Answer.of("Неверные данные. Попробуйте снова");
                }
                MJUser mjUser = new MJUser(userId, login, pass);
                MJApiAnswer apiAnswer = mjService.testApi(mjUser);
                if (apiAnswer != MJApiAnswer.OK) {
                    return Answer.of(apiAnswer.toString());
                }
                mjService.saveUser(mjUser);
                return Answer.of("Пользователь успешно сохранен.");
        }

        return Answer.empty();
    }

    private String mapModules(List<Module> modules) {
        StringBuilder builder = new StringBuilder("<b>Ваши модули:\n</b>");
        if (modules.isEmpty()) {
            return builder.append("Нет модулей").toString();
        }
        Map<String, Map<String, String>> map = modules.stream()
                .collect(Collectors.groupingBy(Module::getTitle,
                        LinkedHashMap::new,
                        Collectors.toMap(Module::getNum, module -> module.getValue() + "")));
        map.forEach((title, results) -> {
            builder.append("<i>").append(title).append(":</i>\n");
            List<String> result = Stream.of(M1, M2, C_WORK, CREDIT, EXAM)
                    .map(type -> {
                        String value = results.getOrDefault(type, EMPTY);
                        return value = value.equals("0") ? "?" : value;
                    })
                    .collect(Collectors.toList());
            builder.append(TABLE_HEADER).append("\n")
                    .append(String.format("%s   %-2s  %-1s  %-1s  %-1s", result.get(0),
                            result.get(1), result.get(2), result.get(3), result.get(4)))
                    .append("\n");
        });
        return builder.toString();
    }
}
