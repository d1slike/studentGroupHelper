package ru.disdev;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.disdev.service.FileService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = StudentHelperApplication.class)
@RunWith(SpringRunner.class)
public class ApplicationTests {

    @Autowired
    private FileService fileService;

    @Test
    public void checkFileVkCollecting() throws IOException {
        Map<String, String> attachments = new HashMap<String, String>() {{
            put("", "https://pp.vk.me/c836638/v836638178/172f8/iejMqCysrVI.jpg");
            put("111", "https://vk.com/doc154712544_438540492");
        }};
        fileService.collectVkAttachments(attachments, "test");
    }
}
