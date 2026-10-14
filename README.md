# Catálogo de Produtos

API REST para cadastro e consulta de produtos, com validação de entrada, respostas de erro
padronizadas e cobertura de testes nos três níveis da pirâmide.

## Stack

- Java 25 · Spring Boot 4.1 · Maven
- PostgreSQL 17 em container, gerenciado pelo Docker Compose
- Spring Data JPA / Hibernate
- JUnit 6 · Mockito · AssertJ · H2 (apenas nos testes)

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
| `GET` | `/produtos` | Lista todos os produtos | 200 |
| `GET` | `/produtos/{id}` | Busca um produto por id | 200, 404 |
| `POST` | `/produtos` | Cria um produto | 201 + `Location`, 400 |
| `PUT` | `/produtos/{id}` | Atualiza nome e preço | 200, 400, 404 |

### Exemplo

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

**`existsByNomeAndIdNot` em vez de `existsByNome` na checagem de nome duplicado.**

Sem isso, atualizar um produto mantendo o nome atual seria recusado: a consulta encontraria o
próprio registro e a regra o trataria como conflito. Escolhi resolver na própria query, em vez
de usar `existsByNome` e comparar o nome atual dentro do service, porque "ignore este id" é uma
condição de busca e não uma regra de negócio — o service fica com uma verificação a menos.

O custo é que a semântica do `AndIdNot` só é verificável com banco real: num teste unitário o
dublê devolve o que eu mandar e a consulta nunca executa. Daí o `ProdutoRepositoryTest` com
`@DataJpaTest`.

**Validação de formato em duas camadas, de propósito.**

O `@Valid` no DTO rejeita requisições HTTP malformadas, com mensagem por campo. Mas ele só
protege a porta HTTP: um importador de CSV ou uma tarefa agendada chamaria o service sem
validação nenhuma. Por isso mantive também a checagem no service, em vez de confiar apenas no
`@Valid` — são defesas de camadas diferentes contra falhas diferentes, e o objeto não deveria
aceitar entrar em estado inválido, venha a chamada de onde vier.

O custo é ter dois lugares para alterar se a regra mudar. Aceitei porque as duas camadas têm
teste: uma divergência quebra alguma coisa em vez de passar despercebida.

## Testes

```bash
./mvnw test
```

Três níveis, com responsabilidades distintas e sem sobreposição:

| Classe | Prova que | Ferramenta |
|---|---|---|
| `ProdutoServiceTest` | a regra de negócio decide certo | JUnit + Mockito |
| `ProdutoRepositoryTest` | a consulta ao banco pergunta certo | `@DataJpaTest` |
| `ProdutoControllerTest` | o contrato HTTP e o JSON estão certos | `@WebMvcTest` |

Os testes de regra rodam em milissegundos, sem subir Spring nem banco. Os outros dois sobem
apenas a fatia necessária do contexto.

Entre eles, `naoDeveVazarCustoInterno` merece nota: ele garante que um campo interno da
entidade não aparece na resposta da API. A entidade tem campos que o contrato não expõe, e essa
separação passa a ser verificada automaticamente em vez de depender de inspeção manual.
