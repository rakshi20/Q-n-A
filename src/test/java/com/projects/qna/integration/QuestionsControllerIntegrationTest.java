package com.projects.qna.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.QnaApplication;
import com.projects.qna.exceptions.ServiceExceptionHandler;
import com.projects.qna.model.Question;
import com.projects.qna.model.User;
import com.projects.qna.service.EntityService;
import com.projects.qna.service.QuestionService;
import com.projects.qna.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {QnaApplication.class})
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuestionsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    QuestionService questionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EntityService entityService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionStatus transactionStatus;

    private Long currentUserIndex, currentQuestionIndex;

    private Question question;

    @BeforeAll
    public void startTransaction() throws Exception{
        transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        currentUserIndex = entityService.getCurrentSequenceValue("users_seq");
        currentQuestionIndex = entityService.getCurrentSequenceValue("questions_seq");
        User user = new User("QnTestUserName", "QnTestPassword", "QnEmail@email.com", "+991234554321");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        user = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);
        question = new Question("Integration Test question", user.getUserId());
        question = questionService.createQuestion(question);
    }

    @AfterAll
    public void rollbackTransaction() {
        if (transactionStatus != null) {
            entityService.resetSequenceValue("users_seq", currentUserIndex);
            entityService.resetSequenceValue("questions_seq", currentQuestionIndex);
            transactionManager.rollback(transactionStatus);
        }
    }

    @Test
    public void verifyApplicationContext() {
        assertThat(webApplicationContext).withFailMessage("Web application context is null").isNotNull();
        assertThat(webApplicationContext.getBean("questionsController")).isNotNull();
    }

    @Test
    public void testGetAllQuestions() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        List<Question> questionsList = Arrays.asList(objectMapper.readValue(responseBody, Question[].class));
        assertThat(questionsList).isNotNull();
        assertThat(questionsList).isInstanceOf(List.class);
        assertThat(questionsList).allMatch(Objects::nonNull);
    }

    @Test
    public void testGetQuestion_questionNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions/{id}", 9999999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        ServiceExceptionHandler.ServiceErrorBody serviceErrorBody = objectMapper.readValue(responseBody,
                ServiceExceptionHandler.ServiceErrorBody.class);
        assertThat(serviceErrorBody.getCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(serviceErrorBody.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    public void testGetQuestion_questionFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions/{id}", question.getQnId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(question.toString());
    }

    @Test
    public void testCreateQuestion_InvalidBody() throws Exception {
        clearInvocations(questionService);
        Question invalidQnBody = new Question("", question.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(invalidQnBody.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(questionService, times(0)).createQuestion(any(Question.class));
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"qn\":\"must not be blank\"");
    }

    @Test
    public void testCreateQuestion_InvalidUserId() throws Exception {
        clearInvocations(questionService);
        Question invalidQnBody = new Question("Nested Test Question", 9999999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(invalidQnBody.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
        verify(questionService, times(1)).createQuestion(any(Question.class));
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        //Map<String, String> errors = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    }
}