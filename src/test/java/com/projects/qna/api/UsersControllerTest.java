package com.projects.qna.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.qna.controllers.UsersController;
import com.projects.qna.model.User;
import com.projects.qna.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsersController.class)
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void verifyApplicationContext() {
        assertThat(mockMvc).isNotNull();
        assertThat(mockMvc.getDispatcherServlet().getWebApplicationContext()).isNotNull();
        Arrays.stream(mockMvc.getDispatcherServlet().getWebApplicationContext().getBeanDefinitionNames())
                .forEach(System.out::println);
    }

    @Test
    public void verifyGetAllUsers() throws Exception {
        List<User> expectedList = Arrays.asList(
                new User(1L, "name1", "pass1", "mail1@mail.com", "+991111122222"),
                new User(2L, "name 2", "123pass", "login@mail.com", "+991234500000")
        );
        when(userService.getAllUsers()).thenReturn(expectedList);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString())
                .isEqualTo(new ObjectMapper().writeValueAsString(expectedList));
    }

    @Test
    public void verifyGetUser_userExists() throws Exception {
        User user = new User(1L, "name1", "pass1", "mail1@mail.com", "+991111122222");
        when(userService.getUser(1L)).thenReturn(user);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(user.toString());
    }

    @Test
    public void verifyGetUser_userDoesNotExist() throws Exception {
        when(userService.getUser(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("User not found");
    }

    @Test
    public void verifyPostUser_validBody() throws Exception {
        User user = new User(1L, "name1", "pass1", "mail1@mail.com", "+991111122222");
        when(userService.createUser(user)).thenReturn(user);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(user.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(user.toString());
        assertThat(mvcResult.getResponse().containsHeader("Location")).isTrue();
        assertThat(mvcResult.getResponse().getHeader("Location")).isEqualTo("/users/1");
    }

    @Test
    public void verifyPostUser_invalidBody() throws Exception {
        User user = new User("", "", "mail1", "+991111122222");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .content(user.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(userService, times(0)).createUser(any(User.class));
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).contains("password", "name", "must not be blank");
        assertThat(responseBody).contains("\"email\":\"must be a well-formed email address\"");
    }

    @Test
    public void verifyUpdateUser_validBody() throws Exception {
        User user = new User(1L, "name1", "pass1", "mail1@mail.com", "+991111122222");
        when(userService.updateUser(1L, user)).thenReturn(user);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", "1")
                        .content(user.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(user.toString());
    }

    @Test
    public void verifyUpdateUser_invalidBody() throws Exception {
        User user = new User(1L, "", "", "mail1", "+991111122222");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", "1")
                        .content(user.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
        verify(userService, times(0)).updateUser(any(Long.class), any(User.class));
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertThat(responseBody).contains("password", "name", "must not be blank");
        assertThat(responseBody).contains("\"email\":\"must be a well-formed email address\"");
    }

    @Test
    public void verifyUpdateUser_IdNotFound() throws Exception {
        User user = new User(1L, "name1", "pass1", "mail1@mail.com", "+991111122222");
        when(userService.updateUser(1L, user))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", "1")
                        .content(user.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("User not found");
    }

    @Test
    public void verifyDeleteUser_IdFound() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("User with id : 1 deleted");
    }

    @Test
    public void verifyDeleteUser_IdNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).deleteUser(1L);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("User not found");
    }
}
