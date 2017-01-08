package ru.disdev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.disdev.entity.mj.MJApiAnswer;
import ru.disdev.entity.mj.MJUser;
import ru.disdev.entity.mj.Module;
import ru.disdev.entity.mj.Semesters;
import ru.disdev.repository.MJUserRepository;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@Service
public class MJService {

    private static final String GET_SEMESTERS = "http://uits-labs.ru:8080/mj/webapi/api2/semesters";
    private static final String GET_MARKS = "http://uits-labs.ru:8080/mj/webapi/api2/marks";

    @Autowired
    private MJUserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;

    public MJUser saveUser(MJUser user) {
        userRepository.save(user);
        return user;
    }

    public MJUser findByUserId(int userId) {
        return userRepository.findOne(userId);
    }

    public MJApiAnswer testApi(MJUser user) {
        ResponseEntity<Semesters> response =
                restTemplate.postForEntity(GET_SEMESTERS, getRequestEntity(user), Semesters.class);
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return MJApiAnswer.BAD_CREDENTIALS;
        } else if (response.getStatusCode() != HttpStatus.OK) {
            return MJApiAnswer.API_ERROR;
        }
        return MJApiAnswer.OK;
    }

    public Semesters getSemesters(MJUser user) {
        return restTemplate.postForObject(GET_SEMESTERS, getRequestEntity(user), Semesters.class);
    }

    public List<Module> getModules(MJUser user, String semester) {
        Module[] modules = restTemplate.postForObject(GET_MARKS, getRequestEntity(user, semester), Module[].class);
        return Arrays.asList(modules);
    }

    private HttpEntity getRequestEntity(MJUser user, String... semester) {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", singletonList(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("student", singletonList(user.getLogin()));
        params.put("password", singletonList(user.getPassword()));
        if (semester.length > 0) {
            params.put("semester", singletonList(semester[0]));
        }
        return new HttpEntity<>(params, headers);
    }
}