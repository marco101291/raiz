package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

interface FunctionalQuestionRepository extends Repository<FunctionalQuestionEntity, String> {

    List<FunctionalQuestionEntity> findAllByOrderBySortOrderAsc();
}
