package com.projects.qna.controllers;

import com.projects.qna.model.Answer;
import com.projects.qna.service.AnswerService;
import lombok.Data;
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
        return ResponseEntity.ok(answerService.getAnswer(id));
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
        return ResponseEntity.ok(answerService.updateAnswer(id, answer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.ok("Answer with id : " + id + " deleted");
    }
}
