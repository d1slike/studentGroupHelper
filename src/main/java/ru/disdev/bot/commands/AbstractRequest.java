package ru.disdev.bot.commands;


import ru.disdev.model.Answer;

import java.util.*;

public abstract class AbstractRequest {

    public final Answer execute(String fullCommand, long chatId, int userId) {
        Request requestAnnotation = getClass().getAnnotation(Request.class);
        String[] requestFormat = requestAnnotation.args();
        if (requestFormat.length > 0) {
            String argList[] = fullCommand.split(" ", requestFormat.length + 1);
            List<String> args = new ArrayList<>(Arrays.asList(argList).subList(1, argList.length));
            return execute(createCommandArgs(requestFormat, args), chatId, userId);
        }
        return execute(new CommandArgs(), chatId, userId);

    }

    private CommandArgs createCommandArgs(String[] requestFormat, List<String> args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.size(); i++) {
            map.put(requestFormat[i], args.get(i));
        }
        return new CommandArgs(map);
    }

    protected abstract Answer execute(CommandArgs args, long chatId, int userId);

}
