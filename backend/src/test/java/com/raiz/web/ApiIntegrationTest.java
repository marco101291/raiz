package com.raiz.web;

import com.raiz.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base de las pruebas de la API: aplicación completa, PostgreSQL real con el catálogo de las
 * migraciones y MockMvc (peticiones HTTP simuladas, sin abrir un puerto). Todas las subclases
 * comparten configuración, así que Spring reutiliza el mismo contexto y el mismo contenedor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
abstract class ApiIntegrationTest {

    @Autowired
    protected MockMvc mvc;

    protected ResultActions postJson(String uri, String json) throws Exception {
        return mvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
