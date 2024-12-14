package com.projects.qna.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.QnaApplication;
import com.projects.qna.exceptions.ServiceExceptionHandler;
import com.projects.qna.model.Answer;
import com.projects.qna.model.Question;
import com.projects.qna.model.User;
import com.projects.qna.service.AnswerService;
import com.projects.qna.service.EntityService;
import com.projects.qna.service.QuestionService;
import com.projects.qna.service.UserService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = QnaApplication.class)
@AutoConfigureMockMvc
public class AnswersControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    EntityService entityService;

    @Autowired
    ObjectMapper objectMapper;

    private Long curUserIndex, curQnIndex, curAnsIndex;

    private User user;

    private Question question;

    private Answer answer;


    public void setup() throws Exception {
        curUserIndex = entityService.getCurrentSequenceValue("users_seq");
        curQnIndex = entityService.getCurrentSequenceValue("questions_seq");
        curAnsIndex = entityService.getCurrentSequenceValue("answers_seq");

        user = userService.createUser(new User(
                "AnsTestUserName", "AnsTestPassword", "AnsTest@email.com", "+999294959391"));
        question = questionService.createQuestion(new Question("AnsTestQuestion", user.getUserId()));
        answer = answerService.createAnswer(new Answer("Test setup answer", question.getQnId(), user.getUserId()));
    }

    public void reset(boolean deleteAnswer, boolean deleteQuestion, boolean deleteUser) throws Exception {
        if (deleteAnswer) answerService.deleteAnswer(answer.getAnsId());
        if (deleteQuestion) questionService.deleteQuestion(question.getQnId());
        if (deleteUser) userService.deleteUser(user.getUserId());

        entityService.resetSequenceValue("users_seq", curUserIndex);
        entityService.resetSequenceValue("questions_seq", curQnIndex);
        entityService.resetSequenceValue("answers_seq", curAnsIndex);
    }

    @Test
    public void verifyApplicationContext() {
        assertThat(webApplicationContext).withFailMessage("Web application context is null").isNotNull();
        assertThat(webApplicationContext.getBean("answersController")).isNotNull();
    }

    @Test
    public void testGetAnswer() throws Exception {
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/answers/{id}", answer.getAnsId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        reset(true, true, true);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(answer.toString());
    }

    @Test
    public void testGetAnswer_invalidAnswerId() throws Exception {
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/answers/{id}", 9999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        reset(true, true, true);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        ServiceExceptionHandler.ServiceErrorBody errorBody = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), ServiceExceptionHandler.ServiceErrorBody.class);
        assertThat(errorBody.getCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(errorBody.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    public void testGetAllAnswers() throws Exception {
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/answers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        reset(true, true, true);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        List<Answer> answersList = Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                Answer[].class));
        assertThat(answersList).isNotNull();
        assertThat(answersList).isInstanceOf(List.class);
        assertThat(answersList).allMatch(Objects::nonNull);
    }

    @Test
    public void testCreateAnswer_IdsDoNotExist() throws Exception {
        setup();
        Answer newAnswer = new Answer("New Test Answer", 9999999L, 999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/answers")
                        .content(newAnswer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        Map<String, String> errors = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(errors.containsKey("SQL state")).isTrue();
        assertThat(errors.get("SQL state")).isEqualTo("23503");
        assertThat(errors.containsKey("server error message")).isTrue();
        assertThat(entityService.getCurrentSequenceValue("answers_seq")).isEqualTo(answer.getAnsId());
        reset(true, true, true);
    }

    @Test
    public void testCreateAnswer_Success() throws Exception {
        setup();
        Answer newAnswer = new Answer("New Test Answer", question.getQnId(), user.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/answers")
                        .content(newAnswer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Answer createdAnswer = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Answer.class);
        newAnswer.setAnsId(curAnsIndex + 2);
        assertThat(Objects.equals(createdAnswer, newAnswer)).isTrue();
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/answers/" + newAnswer.getAnsId());
        answerService.deleteAnswer(createdAnswer.getAnsId());
        reset(true, true, true);
    }

    @Test
    public void testUpdateAnswer_AnsIdDoesNotExist() throws Exception {
        Answer newAnswer = new Answer("New Test Answer", 99999999L, 99999999L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/answers/{id}", 9999999L)
                        .content(newAnswer.toString())
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
    public void testUpdateAnswer_Success() throws Exception {
        setup();
        Answer newAnswer = new Answer("New Test Answer", question.getQnId(), user.getUserId());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/answers/{id}", answer.getAnsId())
                        .content(newAnswer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        Answer updatedAnswer = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Answer.class);
        newAnswer.setAnsId(answer.getAnsId());
        assertThat(Objects.equals(updatedAnswer, newAnswer)).isTrue();
        updatedAnswer = answerService.getAnswer(answer.getAnsId());
        assertThat(Objects.equals(updatedAnswer, newAnswer)).isTrue();
        reset(true, true, true);
    }

    @Test
    public void testDeleteAnswer_IdNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/answers/{id}", 999999L)
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
    public void testDeleteAnswer_Success() throws Exception {
        setup();
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/answers/{id}", answer.getAnsId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        reset(false, true, true);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("Answer with id : " + answer.getAnsId() + " deleted");
    }
}
