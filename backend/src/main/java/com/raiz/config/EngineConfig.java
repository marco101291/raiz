package com.raiz.config;

import com.raiz.domain.assessment.CorrelationEngine;
import com.raiz.domain.port.CommonBilateralCauses;
import com.raiz.domain.port.NerveRootCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El dominio no conoce Spring: el motor se registra aquí como bean, con el catálogo inyectado. */
@Configuration(proxyBeanMethods = false)
public class EngineConfig {

    @Bean
    CorrelationEngine correlationEngine(NerveRootCatalog catalog, CommonBilateralCauses commonBilateralCauses) {
        return new CorrelationEngine(catalog, commonBilateralCauses);
    }
}
