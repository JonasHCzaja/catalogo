package com.jonasczaja.catalogo;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProdutoEndToEndTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Autowired
	ProdutoRepository repository;

	@LocalServerPort
	int porta;

	RestTestClient client;

	@BeforeEach
	void setUp(){
		repository.deleteAll();
		client = RestTestClient.bindToServer()
				.baseUrl("http://localhost:" + porta)
				.build();
	}

	@Test
	void deveCriarProdutoDePontaAPonta(){
		client.post().uri("/produtos")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"nome":"Café E2E","preco":24.90}
						 """)
				.exchange()
				.expectStatus().isCreated();
	}

}
