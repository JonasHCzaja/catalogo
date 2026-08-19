package com.jonasczaja.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return repository.findAll().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody ProdutoRequest request){
        Produto salvo = repository.save(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.from(salvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(ProdutoResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @RequestBody ProdutoRequest request){
        Optional<Produto> encontrado = repository.findById(id);

        if(encontrado.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Produto produto = encontrado.get();
        produto.setNome(request.nome());
        produto.setPreco(request.preco());

        return ResponseEntity.ok(ProdutoResponse.from(repository.save(produto)));

    }

}
