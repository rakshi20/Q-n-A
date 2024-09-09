package com.projects.qna.service;

import com.projects.qna.model.Answer;
import com.projects.qna.model.AnswerRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Answer getAnswer(Long id) {
        return answerRepository.findById(id).orElseThrow();
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public Answer createAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    public Answer updateAnswer(Long id, Answer answer){
        Answer existingAnswer = getAnswer(id);
        answer.setAnsId(id);
        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long id) {
        Answer existingAnswer = getAnswer(id);
        answerRepository.deleteById(id);
    }
}
