package com.projects.qna.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qn_seq_gen")
    @SequenceGenerator(name = "qn_seq_gen", sequenceName = "questions_seq", allocationSize = 1)
    private Long qnId;
    @NotBlank
    private String qn;
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

    public Question(String qn, Long userId) {
        this.qn = qn;
        this.userId = userId;
    }
}
