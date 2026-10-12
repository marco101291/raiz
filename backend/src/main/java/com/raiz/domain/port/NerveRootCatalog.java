package com.raiz.domain.port;

import com.raiz.domain.model.NerveRoot;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: de dónde vienen las raíces. El dominio no sabe que existe JPA. */
public interface NerveRootCatalog {

    List<NerveRoot> findAll();

    Optional<NerveRoot> findByCode(String code);
}
