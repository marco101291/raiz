package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

/** Solo lectura: extiende {@link Repository} y no JpaRepository para no exponer save ni delete. */
interface NerveRootRepository extends Repository<NerveRootEntity, String> {

    List<NerveRootEntity> findAllByOrderBySortOrderAsc();
}
