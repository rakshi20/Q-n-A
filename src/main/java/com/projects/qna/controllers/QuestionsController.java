package com.projects.qna.controllers;

import com.projects.qna.model.Question;
import com.projects.qna.model.User;
import com.projects.qna.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Data
@RequestMapping("/questions")
public class QuestionsController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question found",
                    content = @Content(schema = @Schema(implementation = Question.class))),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<Object> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @GetMapping
    @Operation(summary = "Get all questions",description = "Get all questions in the questions db")
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @PostMapping
    public Question createQuestion(@RequestBody Question question) {
        return questionService.createQuestion(question);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question",description = "Update a question with given ID")
    public ResponseEntity<Object> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        return ResponseEntity.ok(questionService.updateQuestion(id, question));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question with id : " + id + " deleted");
    }
}
