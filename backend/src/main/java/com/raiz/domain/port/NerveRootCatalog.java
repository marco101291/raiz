package com.raiz.domain.port;

import com.raiz.domain.model.NerveRoot;

import java.util.List;
import java.util.Optional;

/** Outbound port: where the roots come from. The domain does not know JPA exists. */
public interface NerveRootCatalog {

    List<NerveRoot> findAll();

    Optional<NerveRoot> findByCode(String code);
}
