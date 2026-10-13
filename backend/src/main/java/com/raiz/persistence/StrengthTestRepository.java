package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

interface StrengthTestRepository extends Repository<StrengthTestEntity, String> {

    List<StrengthTestEntity> findAllByOrderBySortOrderAsc();
}
