package com.raiz.config;

import com.raiz.domain.assessment.CorrelationEngine;
import com.raiz.domain.port.CommonBilateralCauses;
import com.raiz.domain.port.NerveRootCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The domain does not know Spring: the engine is registered here as a bean, with its ports injected. */
@Configuration(proxyBeanMethods = false)
public class EngineConfig {

    @Bean
    CorrelationEngine correlationEngine(NerveRootCatalog catalog, CommonBilateralCauses commonBilateralCauses) {
        return new CorrelationEngine(catalog, commonBilateralCauses);
    }
}
