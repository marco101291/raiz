package com.raiz.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

/** Read-only: extends {@link Repository} rather than JpaRepository so save and delete are not exposed. */
interface NerveRootRepository extends Repository<NerveRootEntity, String> {

    List<NerveRootEntity> findAllByOrderBySortOrderAsc();
}
