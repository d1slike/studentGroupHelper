package ru.disdev.entity.mj;

public enum MJApiAnswer {
    OK("OK"),
    API_ERROR("Не удалось получить данные. Попробуйте позже."),
    BAD_CREDENTIALS("Неверные данные авторизации, пожалуйста введите актуальные данные и попробуйте снова.");
    private final String message;

    MJApiAnswer(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
