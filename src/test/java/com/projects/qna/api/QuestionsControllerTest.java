package com.projects.qna.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.controllers.QuestionsController;
import com.projects.qna.exceptions.ServiceError;
import com.projects.qna.exceptions.ServiceException;
import com.projects.qna.exceptions.ServiceExceptionHandler;
import com.projects.qna.model.Question;
import com.projects.qna.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionsController.class)
public class QuestionsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @Autowired
    private ObjectMapper objectMapper;

//    @BeforeEach
//    public void setup() {
//        assertThat(mockMvc).isNotNull();
//        assertThat(objectMapper).isNotNull();
//    }

    @Test
    public void verifyGetAllQuestions() throws Exception {
        List<Question> expectedList = Arrays.asList(
                new Question("qn1", 1L),
                new Question("What is qn ?", 2L)
        );
        when(questionService.getAllQuestions()).thenReturn(expectedList);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(expectedList));
    }

    @Test
    public void verifyGetQuestion_questionExists() throws Exception {
        Question question = new Question(1L, "qn1", 1L);
        when(questionService.getQuestion(1L)).thenReturn(question);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(question.toString());
    }

    @Test
    public void verifyGetQuestion_questionDoesNotExist() throws Exception {
        when(questionService.getQuestion(1L)).thenThrow(new ServiceException(ServiceError.ENTITY_NOT_FOUND));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/questions/{id}", 1L)
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
    public void verifyCreateQuestion_InvalidBody() throws Exception {
        Question question = new Question("", 1L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(question.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(questionService, times(0)).createQuestion(any(Question.class));
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"qn\":\"must not be blank\"");
    }

    @Test
    public void verifyCreateQuestion_ValidBody() throws Exception {
        Question question = new Question(99L, "New question, isn't it ?", 99L);
        when(questionService.createQuestion(question)).thenReturn(question);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .content(question.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(question.toString());
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/questions/99");
    }

    @Test
    public void verifyUpdateQuestion_InvalidBody() throws Exception {
        Question question = new Question(99L, "", 99L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/questions/{id}", 99L)
                        .content(question.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(questionService, times(0)).updateQuestion(any(Long.class), any(Question.class));
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"qn\":\"must not be blank\"");
    }

    @Test
    public void verifyUpdateQuestion_ValidBody() throws Exception {
        Question question = new Question(99L, "New question, isn't it ?", 99L);
        when(questionService.updateQuestion(99L, question)).thenReturn(question);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/questions/{id}", 99L)
                        .content(question.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(question.toString());
    }

    @Test
    public void verifyDeleteQuestion_IdExists() throws Exception {
        doNothing().when(questionService).deleteQuestion(99L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/questions/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("Question with id : 99 deleted");
    }
}
