package ru.disdev.model;

import com.google.common.collect.ImmutableList;

public class SuperusersList {
    private final ImmutableList<Integer> superusers;

    public SuperusersList(ImmutableList<Integer> superusers) {
        this.superusers = superusers;
    }

    public boolean isSuperUser(int userId) {
        return superusers.contains(userId);
    }

    public ImmutableList<Integer> getSuperusers() {
        return superusers;
    }
}
