package com.projects.qna.service;

import com.projects.qna.exceptions.ServiceError;
import com.projects.qna.exceptions.ServiceException;
import com.projects.qna.model.Answer;
import com.projects.qna.model.AnswerRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class AnswerService {

    private final AnswerRepository answerRepository;

    private final EntityService entityService;

    public Answer getAnswer(Long id) {
        return answerRepository.findById(id).orElseThrow(() -> new ServiceException(ServiceError.ENTITY_NOT_FOUND));
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public Answer createAnswer(Answer answer) {
        Long currentSequenceValue = entityService.getCurrentSequenceValue("answers_seq");
        try {
            return answerRepository.save(answer);
        } catch (Exception e) {
            entityService.resetSequenceValue("answers_seq", currentSequenceValue);
            throw e;
        }
    }

    public Answer updateAnswer(Long id, Answer answer) {
        Answer existingAnswer = getAnswer(id);
        answer.setAnsId(id);
        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long id) {
        Answer existingAnswer = getAnswer(id);
        answerRepository.deleteById(id);
    }
}
