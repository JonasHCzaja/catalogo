package com.jonasczaja.catalogo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
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
