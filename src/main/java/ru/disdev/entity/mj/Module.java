package ru.disdev.entity.mj;

public class Module {
    private double factor;
    private String title;
    private String num;
    private int value;

    public Module() {

    }

    public Module(String title, String num, int value, double factor) {
        this.title = title;
        this.num = num;
        this.value = value;
        this.factor = factor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public double getFactor() {
        return factor;
    }
}
