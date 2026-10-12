package com.raiz.persistence;

import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.port.NerveRootCatalog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adaptador del puerto {@link NerveRootCatalog}. El catálogo solo cambia con una migración, que
 * implica reiniciar la aplicación, así que se carga una vez al arrancar y se guarda inmutable:
 * cada evaluación lee memoria, no la base de datos.
 */
@Component
public class JpaNerveRootCatalog implements NerveRootCatalog {

    private final List<NerveRoot> roots;
    private final Map<String, NerveRoot> byCode;

    JpaNerveRootCatalog(NerveRootRepository repository, PlatformTransactionManager transactionManager) {
        var readOnly = new TransactionTemplate(transactionManager);
        readOnly.setReadOnly(true);
        // Orden anatómico (sort_order): el motor lo usa para desempatar matches con la misma puntuación.
        this.roots = List.copyOf(readOnly.execute(status -> repository.findAllByOrderBySortOrderAsc().stream()
                .map(NerveRootEntity::toDomain)
                .toList()));
        this.byCode = roots.stream().collect(Collectors.toUnmodifiableMap(NerveRoot::code, Function.identity()));
    }

    @Override
    public List<NerveRoot> findAll() {
        return roots;
    }

    @Override
    public Optional<NerveRoot> findByCode(String code) {
        return Optional.ofNullable(byCode.get(code));
    }
}
