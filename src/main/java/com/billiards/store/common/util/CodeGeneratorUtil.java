package com.billiards.store.common.util;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class CodeGeneratorUtil {
    @PersistenceContext
    private EntityManager entityManager;

    public String generateCode(String entityAbbre, String sequenceName) {
        Long nextValue = (Long) entityManager
            .createNativeQuery("SELECT NEXTVAL ('" + sequenceName + "');")
            .getSingleResult();
        return String.format("%s%06d", entityAbbre, nextValue);
    }
}
