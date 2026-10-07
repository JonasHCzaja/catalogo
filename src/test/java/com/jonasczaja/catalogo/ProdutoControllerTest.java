package com.jonasczaja.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(ProdutoController.class)
public class ProdutoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ProdutoService service;

    @Test
    void deveDevolver200ComOProdutoEncontrado(){
        Produto cafe = new Produto("Café", new BigDecimal("24.90"));
        cafe.setId(1L);
        when(service.buscarPorId(1L)).thenReturn(Optional.of(cafe));

        assertThat(mvc.get().uri("/produtos/{id}", 1))
                .hasStatusOk()
                .bodyJson().extractingPath("$.nome").isEqualTo("Café");
    }

    @Test
    void naoDeveVazarCustoInterno(){
        Produto cafe = new Produto("Café", new BigDecimal("24.90"));
        cafe.setId(1L);
        cafe.setCustoInterno(new BigDecimal("15.00"));
        when(service.buscarPorId(1L)).thenReturn(Optional.of(cafe));

        assertThat(mvc.get().uri("/produtos/{id}", 1))
                .hasStatusOk()
                .bodyJson().extractingPath("$").asMap()
                .containsKeys("id", "nome", "preco")
                .doesNotContainKey("custoInterno");
    }

    @Test
    void deveDevolver404QuandoProdutoNaoExiste(){
        when(service.buscarPorId(999L)).thenReturn(Optional.empty());

        assertThat(mvc.get().uri("/produtos/{id}", 999))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveDevolver400ComOsCamposInvalidos() {
        assertThat(mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "", "preco": -5}
                    """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.campos").asMap()
                .containsKeys("nome", "preco");
    }

    @Test
    void deveDevolver201ComLocationHeader(){
        Produto cafe = new Produto();
        cafe.setId(1L);
        when(service.criar(anyString(), any(BigDecimal.class))).thenReturn(cafe);

        assertThat(mvc.post().uri("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome": "Café", "preco": 35.90}
                    """).exchange())
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/produtos/1");
    }

    @Test
    void deveDevolver500QuandoOcorreErroInesperado(){
        when(service.listarTodos()).thenThrow(new RuntimeException("falha simulada"));

        assertThat(mvc.get().uri("/produtos"))
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyJson().extractingPath("$.detail").isEqualTo("Ocorreu um erro inesperado.");
    }
}
