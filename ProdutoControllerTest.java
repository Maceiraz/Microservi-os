package br.com.escola.produtos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void deveListarProdutos() throws Exception {
        mvc.perform(get("/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Teclado"));
    }

    @Test
    void produtoInexistenteDeveRetornar404() throws Exception {
        mvc.perform(get("/produtos/999")).andExpect(status().isNotFound());
    }
}
