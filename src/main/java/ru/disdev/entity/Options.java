package ru.disdev.entity;

import java.util.ArrayList;
import java.util.List;

public class Options {
    private List<Integer> superusers = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public List<Integer> getSuperusers() {
        return superusers;
    }

    public void setSuperusers(List<Integer> superusers) {
        this.superusers = superusers;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
