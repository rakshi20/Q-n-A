package com.projects.qna.controllers;

import com.projects.qna.model.Answer;
import com.projects.qna.service.AnswerService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Data
@RequestMapping("/answers")
public class AnswersController {

    private final AnswerService answerService;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAnswer(@PathVariable Long id) {
        try {
            Answer answer = answerService.getAnswer(id);
            return ResponseEntity.ok(answer);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Answer not found", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @PostMapping
    public Answer createAnswer(@RequestBody Answer answer) {
        return answerService.createAnswer(answer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAnswer(@PathVariable Long id, @RequestBody Answer answer) {
        try {
            Answer updatedAnswer = answerService.updateAnswer(id, answer);
            return ResponseEntity.ok(updatedAnswer);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Answer not found", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAnswer(@PathVariable Long id) {
        try {
            answerService.deleteAnswer(id);
            return ResponseEntity.ok("Answer with id : " + id + " deleted");
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Answer not found", HttpStatus.BAD_REQUEST);
        }
    }
}
