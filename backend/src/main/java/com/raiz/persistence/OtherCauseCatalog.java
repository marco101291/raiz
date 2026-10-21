package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;
import com.raiz.domain.port.CommonBilateralCauses;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;

/**
 * Like the root catalog: loaded once at startup and kept immutable in memory. Also the adapter for
 * {@link CommonBilateralCauses}: a zone has common bilateral causes if any of its other causes is one.
 */
@Component
public class OtherCauseCatalog implements CommonBilateralCauses {

    private final List<OtherCause> causes;

    OtherCauseCatalog(OtherCauseRepository repository, PlatformTransactionManager transactionManager) {
        var readOnly = new TransactionTemplate(transactionManager);
        readOnly.setReadOnly(true);
        // The zones are a lazy collection: they are mapped inside the transaction.
        this.causes = List.copyOf(readOnly.execute(status -> repository.findAllByOrderBySortOrderAsc().stream()
                .map(OtherCauseEntity::toRecord)
                .toList()));
    }

    /** Causes that apply to any of the zones, without duplicates and in catalog order. */
    public List<OtherCause> forZones(Collection<BodyZone> zones) {
        return causes.stream().filter(cause -> zones.stream().anyMatch(cause.zones()::contains)).toList();
    }

    @Override
    public boolean existFor(BodyZone zone) {
        return causes.stream().anyMatch(cause -> cause.commonBilateral() && cause.zones().contains(zone));
    }
}
