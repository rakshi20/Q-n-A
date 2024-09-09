package com.projects.qna.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qn_seq_gen")
    @SequenceGenerator(name = "qn_seq_gen", sequenceName = "questions_seq", allocationSize = 1)
    private Long qnId;
    private String qn;
    private Long userId;
}
