package com.jonasczaja.catalogo;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints =
        @UniqueConstraint(name = "uk_produto_nome", columnNames = "nome"))
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    private BigDecimal custoInterno;

    public Produto() {}

    public Produto(String nome, BigDecimal preco){
        this.nome = nome;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public BigDecimal getPreco(){
        return preco;
    }

    public void setPreco(BigDecimal preco){
        this.preco = preco;
    }

    public BigDecimal getCustoInterno(){return custoInterno;}

    public void setCustoInterno(BigDecimal custoInterno){
        this.custoInterno = custoInterno;
    }

}
