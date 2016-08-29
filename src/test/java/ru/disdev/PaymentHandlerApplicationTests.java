package ru.disdev;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.disdev.entity.Fio;
import ru.disdev.controller.PaymentController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentHandlerApplication.class)
public class PaymentHandlerApplicationTests {

	@Autowired
	private PaymentController controller;
	@Test
	public void testMail() {
		controller.sendMail(new Fio("Комиссаров", "Ян", "Вадимович"), "lilipp1q@yandex.ru", "fjjdfh");
	}

}
