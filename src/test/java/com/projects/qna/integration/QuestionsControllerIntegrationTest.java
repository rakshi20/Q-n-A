package com.projects.qna.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.QnaApplication;
import com.projects.qna.exceptions.ServiceExceptionHandler;
import com.projects.qna.model.Question;
import com.projects.qna.model.User;
import com.projects.qna.service.EntityService;
import com.projects.qna.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {QnaApplication.class})
@AutoConfigureMockMvc
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

    private Long currentUserIndex, currentQuestionIndex;

    private Question question;

    private User user;


    public void setup() throws Exception {
        currentUserIndex = entityService.getCurrentSequenceValue("users_seq");
        currentQuestionIndex = entityService.getCurrentSequenceValue("questions_seq");
        user = new User("QnTestUserName", "QnTestPassword", "QnEmail@email.com", "+991234554321");
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

    public void resetSetup(boolean deleteQuestion) throws Exception {
        if (deleteQuestion) questionService.deleteQuestion(question.getQnId());
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/users/{id}", user.getUserId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        resetSequence();
    }


    public void resetSequence() {
        entityService.resetSequenceValue("users_seq", currentUserIndex);
        entityService.resetSequenceValue("questions_seq", currentQuestionIndex);
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
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions/{id}", question.getQnId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        resetSetup(true);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(question.toString());
    }

    @Test
    public void testCreateQuestion_InvalidBody() throws Exception {
        setup();
        clearInvocations(questionService);
        Question invalidQnBody = new Question("", question.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(invalidQnBody.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        resetSetup(true);
        verify(questionService, times(0)).createQuestion(any(Question.class));
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"qn\":\"must not be blank\"");
    }

    @Test
    public void testCreateQuestion_InvalidUserId() throws Exception {
        Long currentQnIndex = entityService.getCurrentSequenceValue("questions_seq");
        Question invalidQnBody = new Question("Nested Test Question", 9999999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(invalidQnBody.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(questionService, times(1)).createQuestion(any(Question.class));
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Map<String, String> errors = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(errors.containsKey("SQL state")).isTrue();
        assertThat(errors.get("SQL state")).isEqualTo("23503");
        assertThat(errors.containsKey("server error message")).isTrue();
        assertThat(entityService.getCurrentSequenceValue("questions_seq")).isEqualTo(currentQnIndex);
    }

    @Test
    public void testCreateQuestion_Success() throws Exception {
        setup();
        Question newQn = new Question("New Integration Test question", user.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(newQn.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Question createdQuestion = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Question.class);
        newQn.setQnId(currentQuestionIndex + 2);
        assertThat(Objects.equals(newQn, createdQuestion)).isTrue();
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/questions/" + newQn.getQnId());
        questionService.deleteQuestion(createdQuestion.getQnId());
        resetSetup(true);
    }

    @Test
    public void testUpdateQuestion_IdDoesNotExist() throws Exception {
        Question newQn = new Question(9999999999L, "Question ID does not exist", 9999999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/questions/{id}", 9999999999L)
                        .content(newQn.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        ServiceExceptionHandler.ServiceErrorBody errorBody = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ServiceExceptionHandler.ServiceErrorBody.class);
        assertThat(errorBody.getCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(errorBody.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    public void testUpdateQuestion_UserIdDoesNotExist() throws Exception {
        setup();
        Question newQn = new Question(question.getQnId(), "User ID does not exist", 9999999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/questions/{id}", question.getQnId())
                        .content(newQn.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Map<String, String> errors = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(errors.containsKey("SQL state")).isTrue();
        assertThat(errors.get("SQL state")).isEqualTo("23503");
        assertThat(errors.containsKey("server error message")).isTrue();
        assertThat(entityService.getCurrentSequenceValue("questions_seq")).isEqualTo(question.getQnId());
        resetSetup(true);
    }

    @Test
    public void testUpdateQuestion_Success() throws Exception {
        setup();
        Question newQn = new Question("Updated question", user.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/questions/{id}", question.getQnId())
                        .content(newQn.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Question updatedQuestion = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Question.class);
        newQn.setQnId(question.getQnId());
        assertThat(Objects.equals(newQn, updatedQuestion)).isTrue();
        updatedQuestion = questionService.getQuestion(question.getQnId());
        assertThat(Objects.equals(newQn, updatedQuestion)).isTrue();
        resetSetup(true);
    }

    @Test
    public void testDeleteQuestion_IdDoesNotExist() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/questions/{id}", 9999999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        ServiceExceptionHandler.ServiceErrorBody errorBody = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ServiceExceptionHandler.ServiceErrorBody.class);
        assertThat(errorBody.getCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(errorBody.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    public void testDeleteQuestion_Success() throws Exception {
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/questions/{id}", question.getQnId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("Question with id : " + question.getQnId() + " deleted");
        resetSetup(false);
    }
}
