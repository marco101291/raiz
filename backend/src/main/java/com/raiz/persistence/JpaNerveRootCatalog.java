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
 * Adapter for the {@link NerveRootCatalog} port. The catalog only changes with a migration, which means
 * restarting the application, so it is loaded once at startup and kept immutable: each assessment reads
 * memory, not the database.
 */
@Component
public class JpaNerveRootCatalog implements NerveRootCatalog {

    private final List<NerveRoot> roots;
    private final Map<String, NerveRoot> byCode;

    JpaNerveRootCatalog(NerveRootRepository repository, PlatformTransactionManager transactionManager) {
        var readOnly = new TransactionTemplate(transactionManager);
        readOnly.setReadOnly(true);
        // Anatomical order (sort_order): the engine uses it to break ties between matches with the same score.
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
