package ru.disdev.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.disdev.entity.Teacher;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

import static ru.disdev.util.IOUtils.resourceAsStream;

@Service
public class TeacherService {

    @Autowired
    private ObjectMapper mapper;

    private ImmutableMap<String, String> emailsTagMap;
    private ImmutableList<Teacher> teachers;

    @PostConstruct
    private void init() throws IOException {
        List<Teacher> list = mapper.readValue(resourceAsStream("/teachers.json"), new TypeReference<List<Teacher>>() {
        });
        ImmutableMap.Builder<String, String> linksBuilder = ImmutableMap.builder();
        list.forEach(teacher -> {
            if (teacher.getEmail() != null) {
                linksBuilder.put(teacher.getEmail(), teacher.getTag());
            }
        });
        emailsTagMap = linksBuilder.build();
        teachers = ImmutableList.copyOf(list);
    }

    public ImmutableList<Teacher> getTeachers() {
        return teachers;
    }

    public ImmutableMap<String, String> getTeacherEmailTagLinks() {
        return emailsTagMap;
    }
}
