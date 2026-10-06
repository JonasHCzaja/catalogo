package com.jonasczaja.catalogo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service){
        this.service = service;
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return service.listarTodos().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request,
                                                 UriComponentsBuilder uriBuilder){
        Produto salvo = service.criar(request.nome(), request.preco());

        URI uri = uriBuilder
                .path("/produtos/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();
        return ResponseEntity.created(uri).body(ProdutoResponse.from(salvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id){
        return service.buscarPorId(id)
                .map(ProdutoResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ProdutoRequest request){

        Produto atualizado = service.atualizar(id, request.nome(), request.preco());

        return ResponseEntity.ok(ProdutoResponse.from(atualizado));

    }

}
