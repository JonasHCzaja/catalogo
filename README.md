# Catálogo de Produtos
![CI](https://github.com/JonasHCzaja/catalogo/actions/workflows/ci.yml/badge.svg)

API REST para cadastro e consulta de produtos, com validação de entrada, respostas de erro
padronizadas, schema versionado e cobertura de testes nos quatro níveis da pirâmide.

## Stack

- Java 25 · Spring Boot 4.1 · Maven
- PostgreSQL 17 em container, gerenciado pelo Docker Compose
- Spring Data JPA / Hibernate · Flyway para migração de schema
- JUnit 6 · Mockito · AssertJ · Testcontainers

## Como rodar

**Pré-requisito:** Docker em execução.

```bash
git clone https://github.com/JonasHCzaja/catalogo.git
cd catalogo
./mvnw spring-boot:run
```

> Se o Docker não estiver em execução, a aplicação falha ao iniciar com erro
> de conexão ao Postgres. Verifique com `docker ps`.

Não é preciso instalar nem configurar o Postgres. O Spring Boot lê o `compose.yaml`
da raiz, sobe o container ao iniciar a aplicação e o derruba ao parar. A primeira
execução baixa a imagem.

O schema é criado pelas migrações do Flyway em `src/main/resources/db/migration`.
O Hibernate roda com `ddl-auto=validate`: ele não altera o banco, apenas confere se o
schema corresponde às entidades e **falha na inicialização** se divergir.

A API sobe em `http://localhost:8080`.

### Inspecionar o banco

```bash
docker exec -it $(docker ps -qf "ancestor=postgres:17") psql -U catalogo -d catalogo
```

`\dt` lista as tabelas · `\q` sai.

As credenciais do `compose.yaml` são deliberadamente simples: esse banco existe
apenas na máquina de quem desenvolve e não guarda dado real. Em produção,
credencial em arquivo versionado seria falha de segurança.

## Rotas

| Método | Caminho | Descrição | Respostas |
|---|---|---|---|
| `GET` | `/produtos` | Lista paginada de produtos | 200, 400 |
| `GET` | `/produtos/{id}` | Busca um produto por id | 200, 404 |
| `POST` | `/produtos` | Cria um produto | 201 + `Location`, 400, 409 |
| `PUT` | `/produtos/{id}` | Atualiza nome e preço | 200, 400, 404, 409 |
| `DELETE` | `/produtos/{id}` | Remove um produto | 204, 404 |

### Paginação e ordenação

`GET /produtos` aceita os parâmetros padrão do Spring Data:

| Parâmetro | Significa | Padrão |
|---|---|---|
| `page` | página, começando em **zero** | `0` |
| `size` | itens por página, no máximo **100** | `20` |
| `sort` | `campo,asc\|desc` — apenas `id`, `nome` ou `preco` | sem ordem |

```bash
curl -s "localhost:8080/produtos?page=0&size=5&sort=preco,desc"
```

```json
{
  "content": [ { "id": 1, "nome": "Café", "preco": 24.90 } ],
  "page": { "size": 5, "totalElements": 1, "totalPages": 1, "number": 0 }
}
```

Ordenação por um campo fora da lista permitida devolve **400** com a relação dos campos
aceitos. O motivo está nas decisões técnicas.

### Exemplo de criação

```bash
curl -i -X POST localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Café","preco":24.90}'
```

```
HTTP/1.1 201
Location: http://localhost:8080/produtos/1
Content-Type: application/json

{"id":1,"nome":"Café","preco":24.90}
```

### Erros

As respostas de erro seguem a [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html),
com `Content-Type: application/problem+json`. Erros de validação incluem um campo `campos`
com a mensagem de cada campo recusado:

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos estão incorretos",
  "instance": "/produtos",
  "campos": {
    "nome": "O nome é obrigatório",
    "preco": "O preço deve ser maior que zero"
  }
}
```

## Decisões técnicas

**Lista de permissão para os campos de ordenação.**

O resolvedor de `Sort` do Spring Data repassa nomes de propriedade sem validar contra o modelo
de domínio — a documentação avisa que a verificação é responsabilidade da aplicação. Sem uma
lista, `?sort=custoInterno,desc` funcionava: o cliente não conseguia *ler* esse campo, porque o
DTO não o expõe, mas conseguia **ordenar por ele** — e com duas requisições em direções opostas,
inferir o ranking completo de custos. O dado não vazava; a informação vazava.

Escolhi uma lista de **permissão** em vez de proibição porque permissão falha fechada: um campo
sensível adicionado amanhã não é ordenável até alguém decidir que é. A mensagem de erro informa
os campos aceitos em vez de ecoar o recusado, para não permitir enumerar colunas por tentativa.

O custo é manter a lista a cada campo novo que deva ser ordenável.

**`existsByNomeAndIdNot` em vez de `existsByNome` na checagem de nome duplicado.**

Sem isso, atualizar um produto mantendo o nome atual seria recusado: a consulta encontraria o
próprio registro e a regra o trataria como conflito. Escolhi resolver na própria query, em vez
de usar `existsByNome` e comparar o nome atual dentro do service, porque "ignore este id" é uma
condição de busca e não uma regra de negócio — o service fica com uma verificação a menos.

O custo é que a semântica do `AndIdNot` só é verificável com banco real: num teste unitário o
dublê devolve o que eu mandar e a consulta nunca executa. Daí o `ProdutoRepositoryTest` rodar
contra Postgres via Testcontainers.

**Unicidade de nome garantida também pelo banco.**

A verificação no service tem uma janela: duas requisições simultâneas passam ambas pelo
`existsByNomeAndIdNot` antes de qualquer inserção, e as duas gravam. Nenhum teste unitário
encontra isso, porque testes rodam em sequência. Escolhi declarar `unique` no schema em vez de
confiar apenas no código, porque só o banco serializa.

As duas camadas têm papéis distintos e nenhuma é redundante: a checagem no service devolve 400
com mensagem clara, e a restrição do banco fecha a corrida devolvendo 409. O custo é a regra
aparecer em dois lugares.

**Validação de formato em duas camadas, de propósito.**

O `@Valid` no DTO rejeita requisições HTTP malformadas, com mensagem por campo. Mas ele só
protege a porta HTTP: um importador de CSV ou uma tarefa agendada chamaria o service sem
validação nenhuma. Por isso mantive também a checagem no service, em vez de confiar apenas no
`@Valid` — são defesas de camadas diferentes contra falhas diferentes, e o objeto não deveria
aceitar entrar em estado inválido, venha a chamada de onde vier.

O custo é ter dois lugares para alterar se a regra mudar. Aceitei porque as duas camadas têm
teste: uma divergência quebra alguma coisa em vez de passar despercebida.

**`PagedModel` em vez de serializar `Page`.**

A equipe do Spring Data recomenda contra devolver `Page` diretamente: `PageImpl` é um tipo de
domínio interno, e mudanças nele podem alterar a resposta JSON de forma incompatível. É o mesmo
argumento que separa o DTO da entidade, aplicado a uma classe de framework — qualquer tipo
interno no contrato público é a mesma dívida.

O custo é uma etapa de embrulho no controller e um nível a mais no JSON.

## Testes

```bash
./mvnw test
```

Quatro níveis, com responsabilidades distintas e sem sobreposição:

| Classe | Prova que | Ferramenta |
|---|---|---|
| `ProdutoServiceTest` | a regra de negócio decide certo | JUnit + Mockito |
| `ProdutoRepositoryTest` | a consulta ao banco pergunta certo | `@DataJpaTest` + Testcontainers |
| `ProdutoControllerTest` | o contrato HTTP e o JSON estão certos | `@WebMvcTest` |
| `ProdutoEndToEndTest` | a cadeia completa funciona junta | `@SpringBootTest` + Testcontainers |

Os testes de regra rodam em milissegundos, sem subir Spring nem banco. Os dois que usam
Testcontainers sobem um Postgres real — a mesma imagem do `compose.yaml` — e com isso as
migrações do Flyway são aplicadas do zero a cada execução, o que faz a CI validar o schema
em cada push.

Dois testes merecem nota:

- **`naoDeveVazarCustoInterno`** garante que um campo interno da entidade não aparece na resposta
  da API. A entidade tem campos que o contrato não expõe, e essa separação passa a ser verificada
  automaticamente em vez de depender de inspeção manual.
- **`deveRecusarOrdenacaoPorCampoNaoPermitido`** protege a defesa descrita nas decisões técnicas.
  É o tipo de verificação que alguém remove sem perceber ao refatorar, reabrindo o vazamento.

`custoInterno` é um campo interno, usado para margem, e não é exposto pela API em nenhuma rota
nem aceito como critério de ordenação.