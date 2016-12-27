package ru.disdev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.entity.Options;
import ru.disdev.util.IOUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class OptionsService {
    @Autowired
    private ObjectMapper mapper;

    private ImmutableSet<Integer> superusers;
    private ImmutableSet<String> subjectTags;

    @PostConstruct
    private void init() throws IOException {
        Options options = mapper.readValue(IOUtils.resourceAsStream("/options.json"), Options.class);
        superusers = ImmutableSet.copyOf(options.getSuperusers());
        subjectTags = ImmutableSet.copyOf(options.getTags());
    }

    public ImmutableSet<String> getSubjectTags() {
        return subjectTags;
    }

    public boolean isSuperUser(int userId) {
        return superusers.contains(userId);
    }
}
