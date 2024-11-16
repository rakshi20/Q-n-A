package com.projects.qna.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntityService {

    @Autowired
    EntityManager entityManager;

    public Long getCurrentSequenceValue(String sequenceName) {
        Query query = entityManager.createNativeQuery("SELECT last_value from " + sequenceName);
        return (long) query.getSingleResult();
    }

    @Transactional
    public void resetSequenceValue(String sequenceName, Long originalVal) {
        Query query = entityManager.createNativeQuery("SELECT setval(?, ?)");
        query.setParameter(1, sequenceName);
        query.setParameter(2, originalVal);
        Object result = query.getSingleResult();
    }
}
