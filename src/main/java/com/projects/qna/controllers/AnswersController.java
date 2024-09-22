package com.projects.qna.controllers;

import com.projects.qna.model.Answer;
import com.projects.qna.service.AnswerService;
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
@RequestMapping("/answers")
public class AnswersController {

    private final AnswerService answerService;

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer found",
                    content = @Content(schema = @Schema(implementation = Answer.class))),
            @ApiResponse(responseCode = "404", description = "Answer not found")
    })
    public ResponseEntity<Object> getAnswer(@PathVariable Long id) {
        return ResponseEntity.ok(answerService.getAnswer(id));
    }

    @GetMapping
    @Operation(summary = "Get all answers",description = "Get all answers in the answers db")
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @PostMapping
    public Answer createAnswer(@RequestBody Answer answer) {
        return answerService.createAnswer(answer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a answer",description = "Update a answer with given ID")
    public ResponseEntity<Object> updateAnswer(@PathVariable Long id, @RequestBody Answer answer) {
        return ResponseEntity.ok(answerService.updateAnswer(id, answer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.ok("Answer with id : " + id + " deleted");
    }
}
