package ru.disdev.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.entity.mail.Teacher;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

import static ru.disdev.util.IOUtils.resourceAsStream;

@Service
public class TeacherService {

    @Autowired
    private ObjectMapper mapper;

    private ImmutableSet<String> subjectTags;
    private ImmutableMap<String, String> emailsTagMap;
    private ImmutableList<Teacher> teachers;

    @PostConstruct
    private void init() throws IOException {
        List<Teacher> list = mapper.readValue(resourceAsStream("/teachers.json"), new TypeReference<List<Teacher>>() {
        });
        ImmutableSet.Builder<String> tagsBuilder = ImmutableSet.builder();
        ImmutableMap.Builder<String, String> linksBuilder = ImmutableMap.builder();
        list.forEach(teacher -> {
            tagsBuilder.add(teacher.getTag());
            linksBuilder.put(teacher.getEmail(), teacher.getTag());
        });
        subjectTags = tagsBuilder.build();
        emailsTagMap = linksBuilder.build();
        teachers = ImmutableList.copyOf(list);
    }

    public ImmutableList<Teacher> getTeachers() {
        return teachers;
    }

    public ImmutableSet<String> getSubjectTags() {
        return subjectTags;
    }

    public ImmutableMap<String, String> getEmailsTagLinks() {
        return emailsTagMap;
    }
}
