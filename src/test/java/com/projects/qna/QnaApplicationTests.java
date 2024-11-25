package com.projects.qna;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class QnaApplicationTests {

    @Test
    void contextLoads(WebApplicationContext context) {
        Assert.notNull(context, "Null web application context");
        //Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
        Assert.notNull(context.getBean("qnaApplication"), "QnA Application bean not found");
    }

}
