package com.projects.qna.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ans_seq_gen")
    @SequenceGenerator(name = "ans_seq_gen", sequenceName = "answers_seq", allocationSize = 1)
    private Long ansId;
    @NotBlank
    private String ans;
    private Long qnId;
    private Long userId;

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error processing in json format";
        }
    }

    public Answer(String ans, Long qnId, Long userId) {
        this.ans = ans;
        this.qnId = qnId;
        this.userId = userId;
    }
}
