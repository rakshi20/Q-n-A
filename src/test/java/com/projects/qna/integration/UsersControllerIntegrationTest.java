package com.projects.qna.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.QnaApplication;
import com.projects.qna.model.User;
import com.projects.qna.service.EntityService;
import com.projects.qna.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {QnaApplication.class})
@AutoConfigureMockMvc
@Transactional
public class UsersControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EntityService entityService;

    @BeforeEach
    public void setup() {
        //this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void verifyApplicationContext() {
        assertThat(webApplicationContext).withFailMessage("Web application context is null").isNotNull();
        assertThat(webApplicationContext.getBean("usersController")).isNotNull();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        List<User> usersList = Arrays.asList(objectMapper.readValue(responseBody, User[].class));
        assertThat(usersList).isNotNull();
        assertThat(usersList).isInstanceOf(List.class);
        assertThat(usersList).allMatch(Objects::nonNull);
    }

    @Test
    public void testGetUser_userFound() throws Exception {
        Long currentIndex = entityService.getCurrentSequenceValue("users_seq");
        User user = userService.createUser(
                new User("userName", "password", "email@email.com", "+999999999999"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        entityService.resetSequenceValue("users_seq", currentIndex);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(objectMapper.writeValueAsString(user));
    }

    @Test
    public void testGetUser_userNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/{id}", 9999999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("User not found");
    }

    @Test
    public void testCreateUser() throws Exception {
        Long currentIndex = entityService.getCurrentSequenceValue("users_seq");
        User user = new User("userName", "password", "email@email.com", "+999999999999");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        entityService.resetSequenceValue("users_seq", currentIndex);
        user.setUserId(currentIndex + 1);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(objectMapper.writeValueAsString(user));
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/users/" + user.getUserId());
    }

    @Test
    public void testCreateUser_invalidBody() throws Exception {
        User user = new User("", "", "mail1", "+991111122222");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).contains("password", "name", "must not be blank");
        assertThat(responseBody).contains("\"email\":\"must be a well-formed email address\"");
    }


    @Test
    public void testUpdateUser_userExists() throws Exception {
        Long currentIndex = entityService.getCurrentSequenceValue("users_seq");
        User user = userService.createUser(
                new User("userName", "password", "email@email.com", "+999999999999"));
        user.setPassword("newPassword");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", user.getUserId())
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        entityService.resetSequenceValue("users_seq", currentIndex);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo(objectMapper.writeValueAsString(user));
    }

    @Test
    public void testUpdateUser_userDoesNotExist() throws Exception {
        User user = userService.createUser(
                new User(99999999L, "userName", "password", "email@email.com", "+999999999999"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", 99999999L)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("User not found");
    }

    @Test
    public void testUpdateUser_invalidBody() throws Exception {
        User user = new User(1L, "", "", "mail1", "+991111122222");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", 99999999L)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).contains("password", "name", "must not be blank");
        assertThat(responseBody).contains("\"email\":\"must be a well-formed email address\"");
    }

    @Test
    public void testDeleteUser_userExists() throws Exception {
        Long currentIndex = entityService.getCurrentSequenceValue("users_seq");
        User user = userService.createUser(
                new User("userName", "password", "email@email.com", "+999999999999"));
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/users/{id}", user.getUserId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        entityService.resetSequenceValue("users_seq", currentIndex);
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("User with id : " + user.getUserId() + " deleted");
    }

    @Test
    public void testDeleteUser_userDoesNotExist() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/users/{id}", 999999L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("User not found");
    }
}
