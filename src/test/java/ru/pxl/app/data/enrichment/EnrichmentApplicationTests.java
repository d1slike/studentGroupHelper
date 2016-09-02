package ru.pxl.app.data.enrichment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.disdev.PaymentHandlerApplication;
import ru.disdev.VkApi;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentHandlerApplication.class)
public class EnrichmentApplicationTests {


    @Autowired
    private VkApi api;

    @Test
    public void testHttpPost() {
        int status = api.post(76299169, "test");
        status++;
        //api.requestAccessToken();
    }

}
