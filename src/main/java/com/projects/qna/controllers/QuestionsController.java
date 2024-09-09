package com.projects.qna.controllers;

import com.projects.qna.model.Question;
import com.projects.qna.service.QuestionService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Data
@RequestMapping("/questions")
public class QuestionsController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getQuestion(@PathVariable Long id) {
        try {
            Question question = questionService.getQuestion(id);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Question not found", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @PostMapping
    public Question createQuestion(@RequestBody Question question) {
        return questionService.createQuestion(question);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        try {
            Question updatedQuestion = questionService.updateQuestion(id, question);
            return ResponseEntity.ok(updatedQuestion);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Question not found", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok("Question with id : " + id + " deleted");
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Question not found", HttpStatus.BAD_REQUEST);
        }
    }
}
