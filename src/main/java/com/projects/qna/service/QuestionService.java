package com.projects.qna.service;

import com.projects.qna.exceptions.ServiceError;
import com.projects.qna.exceptions.ServiceException;
import com.projects.qna.model.Question;
import com.projects.qna.model.QuestionRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class QuestionService {

    private final QuestionRepository questionRepository;

    private final EntityService entityService;

    public Question getQuestion(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new ServiceException(ServiceError.ENTITY_NOT_FOUND));
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question createQuestion(Question question) {
        Long currentSequenceValue = entityService.getCurrentSequenceValue("questions_seq");
        try {
            return questionRepository.save(question);
        } catch (Exception e) {
            entityService.resetSequenceValue("questions_seq", currentSequenceValue);
            throw e;
        }
    }

    public Question updateQuestion(Long id, Question question) {
        Question existingQuestion = getQuestion(id);
        question.setQnId(id);
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        Question existingQuestion = getQuestion(id);
        questionRepository.deleteById(id);
    }
}
