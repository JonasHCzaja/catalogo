package com.jonasczaja.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProdutoResponse> criar(@RequestBody ProdutoRequest request){
        Produto salvo = service.criar(request.nome(), request.preco());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.from(salvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id){
        return service.buscarPorId(id)
                .map(ProdutoResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @RequestBody ProdutoRequest request){

        Produto atualizado = service.atualizar(id, request.nome(), request.preco());

        return ResponseEntity.ok(ProdutoResponse.from(atualizado));

    }

}
