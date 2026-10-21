package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

interface OtherCauseRepository extends Repository<OtherCauseEntity, String> {

    List<OtherCauseEntity> findAllByOrderBySortOrderAsc();
}
