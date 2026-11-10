package com.jonasczaja.catalogo;

import org.hibernate.type.OrderedMapType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    private static final List<String> ORDENAVEIS = List.of("id", "nome", "preco");

    public ProdutoService(ProdutoRepository repository){
        this.repository = repository;
    }

    private void validarOrdenacao(Pageable pageable) {
        pageable.getSort().forEach(ordem -> {
            if (!ORDENAVEIS.contains(ordem.getProperty())) {
                throw new RegraDeNegocioException(
                        "Ordenação permitida apenas por: " + String.join(", ", ORDENAVEIS));
            }
        });
    }

    public Page<Produto> listarTodos(Pageable pageable){
        validarOrdenacao(pageable);
        return repository.findAll(pageable);
    }

    public Optional<Produto> buscarPorId(Long id){
        return repository.findById(id);
    }

    public Produto criar(String nome, BigDecimal preco){

        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0){
            throw new RegraDeNegocioException("Preço deve ser maior que zero");
        }
        if (repository.existsByNome(nome)){
            throw new RegraDeNegocioException("Já existe produto com o nome " + nome);
        }
        return repository.save(new Produto(nome, preco));
    }

    public Produto atualizar(Long id, String nome, BigDecimal preco){
        Optional<Produto> encontrado = repository.findById(id);

        if(encontrado.isEmpty()){
            throw new ProdutoNaoEncontradoException("Produto com id " + id + " nao encontrado");
        }
        if(preco == null || preco.compareTo(BigDecimal.ZERO) <= 0){
            throw new RegraDeNegocioException("Preço deve ser maior que zero");
        }
        if(repository.existsByNomeAndIdNot(nome, id)){
            throw new RegraDeNegocioException("Já existe produto com o nome " + nome);
        }

        Produto produto = encontrado.get();
        produto.setNome(nome);
        produto.setPreco(preco);

        return repository.save(produto);
    }


    public void deletar(Long id){
        if(!repository.existsById(id)){
            throw new ProdutoNaoEncontradoException("Produto com id " + id + " não encontrado");
        }
        repository.deleteById(id);
    }
}
