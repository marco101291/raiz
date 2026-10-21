package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

interface ResultTextRepository extends Repository<ResultTextEntity, String> {

    List<ResultTextEntity> findAll();
}
