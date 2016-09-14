package ru.pxl.app.data.enrichment;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.disdev.model.TimeTable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class EnrichmentApplicationTests {
    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TimeTable timeTable = mapper.readValue(new File("time_table.json"), TimeTable.class);
        timeTable.getTo(LocalDate.of(2016, 9, 13)).forEach((integer, s) -> {
            System.out.println(integer + " : " + s);
        });
        System.out.println("------------");
        //String lesson = timeTable.getNextLesson(LocalDateTime.of(2016, 9, 13, 00, 26, 0, 0));
        //System.out.println(lesson);
    }
}
