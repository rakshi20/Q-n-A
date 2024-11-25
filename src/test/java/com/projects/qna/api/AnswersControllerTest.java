package com.projects.qna.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.controllers.AnswersController;
import com.projects.qna.model.Answer;
import com.projects.qna.service.AnswerService;
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

@WebMvcTest(AnswersController.class)
public class AnswersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnswerService answerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void verifyGetAllAnswers() throws Exception {
        List<Answer> answerList = Arrays.asList(
                new Answer("the answer", 99L, 99L),
                new Answer("another", 197L, 80L),
                new Answer("newOne", 46L, 32L)
        );
        when(answerService.getAllAnswers()).thenReturn(answerList);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/answers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(answerList));
    }

    @Test
    public void verifyGetAnswer_answerExists() throws Exception {
        Answer answer = new Answer("the answer", 99L, 99L);
        when(answerService.getAnswer(1L)).thenReturn(answer);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/answers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(answer.toString());
    }

    @Test
    public void verifyCreateAnswer_InvalidBody() throws Exception {
        Answer answer = new Answer("", 99L, 99L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/answers")
                        .content(answer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(answerService, times(0)).createAnswer(any(Answer.class));
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"ans\":\"must not be blank\"");
    }

    @Test
    public void verifyCreateAnswer_ValidBody() throws Exception {
        Answer answer = new Answer(12L, "The answer", 99L, 99L);
        when(answerService.createAnswer(answer)).thenReturn(answer);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/answers")
                        .content(answer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(answer.toString());
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/answers/12");
    }

    @Test
    public void verifyUpdateAnswer_InvalidBody() throws Exception {
        Answer answer = new Answer(99L, "", 99L, 99L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/answers/{id}", 99L)
                        .content(answer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(answerService, times(0)).updateAnswer(any(Long.class), any(Answer.class));
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"ans\":\"must not be blank\"");
    }

    @Test
    public void verifyUpdateAnswer_ValidBody() throws Exception {
        Answer answer = new Answer(99L, "The answer", 99L, 99L);
        when(answerService.updateAnswer(99L, answer)).thenReturn(answer);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/answers/{id}", 99L)
                        .content(answer.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(answer.toString());
    }

    @Test
    public void verifyDeleteAnswer_IdExists() throws Exception {
        doNothing().when(answerService).deleteAnswer(99L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/answers/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("Answer with id : 99 deleted");
    }
}
