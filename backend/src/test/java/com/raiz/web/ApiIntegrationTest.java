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
 * Base for the API tests: full application, real PostgreSQL with the catalog from the migrations, and
 * MockMvc (simulated HTTP requests, no open port). All subclasses share configuration, so Spring reuses
 * the same context and the same container.
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
