package com.projects.qna.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "answers")
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ans_seq_gen")
    @SequenceGenerator(name = "ans_seq_gen", sequenceName = "answers_seq", allocationSize = 1)
    private Long ansId;
    @NotBlank
    private String ans;
    private Long qnId;
    private Long userId;
}
