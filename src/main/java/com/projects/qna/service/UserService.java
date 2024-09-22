package com.projects.qna.service;

import com.projects.qna.model.User;
import com.projects.qna.model.UserRepository;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Data
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUser(id);
        user.setUserId(id);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User existingUser = getUser(id);
        userRepository.deleteById(id);
    }
}
