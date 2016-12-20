package ru.disdev.entity;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

public class Answer {

    private static final Answer EMPTY = new Answer();

    private String text;
    private ReplyKeyboard keyboard;
    private boolean withHtml;

    public Answer(String text) {
        this.text = text;
    }

    public Answer(String text, ReplyKeyboard keyboard) {
        this.text = text;
        this.keyboard = keyboard;
    }

    private Answer() {

    }

    public ReplyKeyboard getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(ReplyKeyboard keyboard) {
        this.keyboard = keyboard;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Answer withHtml() {
        withHtml = true;
        return this;
    }

    public static Answer of(String text) {
        return new Answer(text);
    }

    public static Answer of(String text, ReplyKeyboard keyboard) {
        return new Answer(text, keyboard);
    }

    public static Answer nothing() {
        return EMPTY;
    }

    public boolean isWithHtml() {
        return withHtml;
    }
}
