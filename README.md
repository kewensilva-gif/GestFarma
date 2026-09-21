# GestFarma — Gerenciamento Farmácia — Fullstack

Sistema fullstack de gerenciamento de farmácia organizado como **monorepo**, com uma API REST em Spring Boot e uma SPA em React. Oferece controle completo de produtos, categorias, clientes, funcionários, vendas e autenticação JWT, com páginas personalizadas por papel de acesso.

---

## Estrutura do Repositório

```
farmacia-springboot/
├── backend/    ← API REST (Spring Boot 4 + Java 21 + PostgreSQL)
└── frontend/   ← SPA (React 19 + TypeScript + Tailwind CSS + MUI)
```

---

## Tecnologias

### Backend

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Security | 7.0.3 |
| Spring Data JPA | — |
| PostgreSQL | — |
| Flyway | — |
| JWT (jjwt) | 0.12.7 |
| MapStruct | — |
| Springdoc OpenAPI | 2.8.14 |
| Lombok | — |
| Maven | — |

### Frontend

| Tecnologia | Versão |
|---|---|
| React | 19 |
| TypeScript | 5 |
| Vite | 6 |
| Tailwind CSS | 3 |
| MUI (Material UI) | 7 |
| React Router | 7 |
| Axios | — |
| React Hook Form | — |
| Zod | — |

---

## Funcionalidades

### Backend

- **Soft Delete** — Produtos, vendas e categorias usam exclusão lógica (campo `enabled`).
- **Gestão de Estoque** — Débito automático na venda; restauração ao cancelar.
- **Controle de Validade** — Bloqueia venda de produtos vencidos; endpoint dedicado para listar expirados.
- **Alerta de Estoque Baixo** — Endpoint com limite configurável.
- **Cálculo Automático de Totais** — Total calculado a partir de (itens × preço unitário) − desconto.
- **Snapshot de Preço** — Itens de venda capturam o preço unitário no momento da venda.
- **Validações em Cascata** — Categoria não pode ser desativada se possuir produtos ativos; funcionário demitido não pode registrar vendas.
- **Autenticação JWT** — HMAC-SHA com expiração configurável; login por username ou email.
- **Controle de Acesso por Papel** — 3 papéis: ADMIN, EMPLOYEE, CUSTOMER.
- **Registro Administrativo** — Endpoint transacional que cria usuário + pessoa + funcionário/cliente em uma operação.
- **Documentação Interativa** — Swagger UI em `/swagger-ui.html`.

### Frontend

- **Autenticação completa** — Login, registro público (CUSTOMER) e logout com token JWT armazenado em `localStorage`. Interceptor Axios injeta o Bearer token automaticamente em todas as requisições, com redirecionamento para `/login` em caso de 401.
- **Páginas por papel** — Dashboard, sidebar e permissões de UI adaptadas ao papel do usuário autenticado.
- **Proteção de rotas** — `ProtectedRoute` bloqueia acesso não autorizado e exibe página 403 para roles insuficientes.
- **CRUD completo** — Todas as entidades possuem tabela com busca/filtros, formulário em dialog, confirmação de exclusão e feedback via Snackbar.
- **Skeleton de carregamento** — Linhas de tabela com `MUI Skeleton` durante requisições.
- **Validação de formulários** — React Hook Form + Zod com erros inline em todos os campos.

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java | 21 |
| Maven | 3.9 |
| PostgreSQL | — (em execução) |
| Node.js | 18 |
| npm | 9 |

---

## Configuração

### Backend — Variáveis de Ambiente

Crie `backend/.env` com base em `backend/.env.example`:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=farmacia
DB_USER=seu_usuario
DB_PASSWORD=sua_senha

JWT_SECRET=sua_chave_secreta_base64
JWT_EXPIRATION_MS=86400000
```

> O projeto usa [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) para carregar `.env` automaticamente.

### Frontend — Variáveis de Ambiente

Crie `frontend/.env` (já incluso no projeto):

```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## Executando o Projeto

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

A SPA estará disponível em `http://localhost:5173`.

---

## Executando com Kubernetes (Minikube)

A aplicação conta com manifestos completos na pasta `k8s/` para orquestração. Para subir o ambiente:

1. **Inicie o Minikube:**
   ```bash
   minikube start
   ```

2. **Aponte o Docker para o Minikube e faça o build das imagens:**
   Como os manifestos usam `imagePullPolicy: Never`, as imagens precisam estar disponíveis localmente no cluster.
   ```bash
   eval $(minikube docker-env)
   docker build -t gestfarma-backend:latest ./backend
   docker build -t gestfarma-frontend:latest ./frontend
   ```

3. **Aplique os manifestos recursivamente:**
   ```bash
   kubectl apply -R -f k8s/
   ```

4. **Acesse as aplicações:**
   O backend e o frontend estão expostos via `NodePort`. Use o comando abaixo para obter a URL de acesso:
   ```bash
   minikube service gestfarma-frontend --url
   minikube service gestfarma-backend --url
   ```

---

## Banco de Dados

O Flyway executa as migrations automaticamente ao iniciar o backend:

| Migration | Descrição |
|---|---|
| `V1__create_tables.sql` | ENUM `payment_method`, extensão `pgcrypto` e todas as tabelas |
| `V2__seed_roles.sql` | Inserção dos papéis padrão: ADMIN, EMPLOYEE, CUSTOMER |
| `V3__refactor_user_role_to_single.sql` | Refatoração para papel único: adiciona `role_id` em user, remove `user_role` |
| `V4__seed_data.sql` | Dados de desenvolvimento (usuários, produtos, vendas) |
| `V5__add_enabled_column_soft_delete.sql` | Coluna `enabled` em product, sale e category |

---

## Dados de Desenvolvimento

A migration `V4` insere os seguintes usuários para testes:

| Usuário | Senha | Papel |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `joao.silva` | `func123` | EMPLOYEE |
| `maria.santos` | `func123` | EMPLOYEE |
| `carlos.oliveira` | `cli123` | CUSTOMER |
| `ana.souza` | `cli123` | CUSTOMER |
| `pedro.lima` | `cli123` | CUSTOMER |
| `lucia.ferreira` | `cli123` | CUSTOMER |

---

## Documentação da API

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## Modelo de Dados

```
User (UUID PK) ──ManyToOne──► Role (UUID PK)
     │
     └── OneToOne ──► Person (BIGINT PK)
                          ├── OneToOne ──► Employee (salary, hiring/termination dates)
                          └── OneToOne ──► Customer (registration date)

Category ◄── ManyToOne ── Product (barcode, stock, expiration, enabled)
                              │
Sale ── OneToMany ──► SaleProduct ── ManyToOne ──► Product
  │                     (quantity, unit_price snapshot)
  ├── ManyToOne ──► Employee
  └── ManyToOne ──► Customer (nullable — venda anônima)
```

**Métodos de pagamento:** `CREDITCARD`, `DEBITCARD`, `PIX`, `CASH`

---

## Controle de Acesso

| Papel | Descrição |
|---|---|
| `ADMIN` | Acesso total ao sistema |
| `EMPLOYEE` | Gerenciamento de vendas e clientes; leitura de produtos e categorias |
| `CUSTOMER` | Leitura de produtos e categorias |

| Recurso | GET | POST / PUT | DELETE |
|---|---|---|---|
| `/api/auth/**` | — | Público | — |
| `/api/products/**`, `/api/categories/**` | Autenticado | ADMIN | ADMIN |
| `/api/sales/**`, `/api/sale-products/**` | ADMIN, EMPLOYEE | ADMIN, EMPLOYEE | ADMIN, EMPLOYEE |
| `/api/customers/**` | ADMIN, EMPLOYEE, CUSTOMER | ADMIN, EMPLOYEE | ADMIN |
| `/api/users/**`, `/api/roles/**`, `/api/employees/**` | ADMIN | ADMIN | ADMIN |
| `/api/admin/**` | ADMIN | ADMIN | ADMIN |
| Demais endpoints | Autenticado | Autenticado | Autenticado |

---

## Endpoints

### Autenticação — `/api/auth`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/register` | Registrar novo usuário (papel CUSTOMER), retorna JWT | Público |
| POST | `/api/auth/login` | Autenticar por username ou email, retorna JWT | Público |

### Registro Administrativo — `/api/admin`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/admin/register` | Criar usuário + pessoa + funcionário/cliente em transação única | ADMIN |

### Produtos — `/api/products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/products` | Listar todos os produtos ativos | Autenticado |
| GET | `/api/products/{id}` | Buscar por ID | Autenticado |
| GET | `/api/products/search/barcode?barcode=` | Buscar por código de barras | Autenticado |
| GET | `/api/products/search/name?name=` | Buscar por nome (ILIKE) | Autenticado |
| GET | `/api/products/category/{categoryId}` | Filtrar por categoria | Autenticado |
| GET | `/api/products/expired` | Listar produtos vencidos | Autenticado |
| GET | `/api/products/low-stock?quantity=10` | Listar com estoque baixo | Autenticado |
| POST | `/api/products` | Criar produto | ADMIN |
| PUT | `/api/products/{id}` | Atualizar produto | ADMIN |
| DELETE | `/api/products/{id}` | Desativar produto (soft delete) | ADMIN |

### Categorias — `/api/categories`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/categories` | Listar todas as categorias ativas | Autenticado |
| GET | `/api/categories/{id}` | Buscar por ID | Autenticado |
| GET | `/api/categories/search/name?name=` | Buscar por nome | Autenticado |
| POST | `/api/categories` | Criar categoria | ADMIN |
| PUT | `/api/categories/{id}` | Atualizar categoria | ADMIN |
| DELETE | `/api/categories/{id}` | Desativar (bloqueado se há produtos ativos) | ADMIN |

### Vendas — `/api/sales`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sales` | Listar todas as vendas ativas | ADMIN, EMPLOYEE |
| GET | `/api/sales/{id}` | Buscar por ID | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/payment-method?paymentMethod=` | Filtrar por método de pagamento | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/price-greater?price=` | Vendas acima do valor | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/price-less?price=` | Vendas abaixo do valor | ADMIN, EMPLOYEE |
| POST | `/api/sales` | Registrar venda (debita estoque) | ADMIN, EMPLOYEE |
| PUT | `/api/sales/{id}` | Atualizar venda | ADMIN, EMPLOYEE |
| DELETE | `/api/sales/{id}` | Cancelar venda (soft delete, restaura estoque) | ADMIN, EMPLOYEE |

### Itens de Venda — `/api/sale-products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sale-products` | Listar todos os itens | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/{id}` | Buscar por ID | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/search/sale/{saleId}` | Itens por venda | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/search/product/{productId}` | Itens por produto | ADMIN, EMPLOYEE |
| DELETE | `/api/sale-products/{id}` | Remover item (restaura estoque) | ADMIN, EMPLOYEE |

### Clientes — `/api/customers`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/customers` | Listar todos os clientes | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/{id}` | Buscar por ID | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/search/after?date=` | Registrados após a data | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/search/before?date=` | Registrados antes da data | ADMIN, EMPLOYEE, CUSTOMER |
| POST | `/api/customers` | Criar cliente | ADMIN, EMPLOYEE |
| PUT | `/api/customers/{id}` | Atualizar cliente | ADMIN, EMPLOYEE |
| DELETE | `/api/customers/{id}` | Remover cliente | ADMIN |

### Funcionários — `/api/employees`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/employees` | Listar todos os funcionários | ADMIN |
| GET | `/api/employees/{id}` | Buscar por ID | ADMIN |
| GET | `/api/employees/active` | Listar funcionários ativos | ADMIN |
| GET | `/api/employees/inactive` | Listar funcionários inativos (demitidos) | ADMIN |
| GET | `/api/employees/search/after?date=` | Contratados após a data | ADMIN |
| GET | `/api/employees/search/before?date=` | Contratados antes da data | ADMIN |
| POST | `/api/employees` | Criar funcionário | ADMIN |
| PUT | `/api/employees/{id}` | Atualizar funcionário | ADMIN |
| DELETE | `/api/employees/{id}` | Remover funcionário | ADMIN |

### Pessoas — `/api/people`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/people` | Listar todas as pessoas | Autenticado |
| GET | `/api/people/{id}` | Buscar por ID | Autenticado |
| GET | `/api/people/search/cpf?cpf=` | Buscar por CPF | Autenticado |
| GET | `/api/people/search/exists/cpf?cpf=` | Verificar existência por CPF | Autenticado |
| POST | `/api/people` | Criar pessoa | Autenticado |
| PUT | `/api/people/{id}` | Atualizar pessoa | Autenticado |
| DELETE | `/api/people/{id}` | Remover pessoa | Autenticado |

### Usuários — `/api/users`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/users` | Listar todos os usuários | ADMIN |
| GET | `/api/users/{id}` | Buscar por UUID | ADMIN |
| GET | `/api/users/search/username?username=` | Buscar por username | ADMIN |
| GET | `/api/users/search/email?email=` | Buscar por email | ADMIN |
| GET | `/api/users/enabled` | Listar usuários ativos | ADMIN |
| GET | `/api/users/disabled` | Listar usuários inativos | ADMIN |
| GET | `/api/users/search/exists/username?username=` | Verificar existência por username | ADMIN |
| GET | `/api/users/search/exists/email?email=` | Verificar existência por email | ADMIN |
| POST | `/api/users` | Criar usuário | ADMIN |
| PUT | `/api/users/{id}` | Atualizar usuário | ADMIN |
| DELETE | `/api/users/{id}` | Remover usuário | ADMIN |

### Papéis — `/api/roles`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/roles` | Listar todos os papéis | ADMIN |
| GET | `/api/roles/{id}` | Buscar por UUID | ADMIN |
| GET | `/api/roles/search/name?name=` | Buscar por nome | ADMIN |
| GET | `/api/roles/search/exists/name?name=` | Verificar existência por nome | ADMIN |
| POST | `/api/roles` | Criar papel | ADMIN |
| PUT | `/api/roles/{id}` | Atualizar papel | ADMIN |
| DELETE | `/api/roles/{id}` | Remover papel (bloqueado se em uso) | ADMIN |

---

## Autenticação JWT

Após o login, inclua o token em todas as requisições protegidas:

```
Authorization: Bearer <token>
```

### Login

```json
POST /api/auth/login
{
  "login": "admin",
  "password": "admin123"
}
```

> O campo `login` aceita tanto username quanto email.

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "email": "admin@farmacia.com"
}
```

### Registro público

```json
POST /api/auth/register
{
  "username": "novousuario",
  "email": "novo@email.com",
  "password": "senha123"
}
```

### Registro administrativo

```json
POST /api/admin/register
{
  "firstName": "Maria",
  "lastName": "Santos",
  "cpf": "12345678901",
  "username": "maria.santos",
  "email": "maria@farmacia.com",
  "password": "senha123",
  "roleName": "EMPLOYEE",
  "hiringDate": "2024-01-15",
  "salary": 3500.00
}
```

---

## Estrutura do Projeto

### Backend

```
backend/src/
├── main/
│   ├── java/com/kewen/GerenciamentoFarmacia/
│   │   ├── config/          # SecurityConfig, SwaggerConfig
│   │   ├── controllers/     # 11 controladores REST
│   │   ├── converters/      # PaymentMethodConverter
│   │   ├── dto/             # DTOs e records (auth, customer, employee, user)
│   │   ├── entities/        # Entidades JPA
│   │   ├── enums/           # PaymentMethodEnum
│   │   ├── mappers/         # MapStruct (Customer, Employee, User)
│   │   ├── repositories/    # Spring Data JPA
│   │   ├── security/        # JwtService, JwtAuthenticationFilter, UserDetailsService
│   │   └── services/        # Regras de negócio
│   └── resources/
│       ├── application.properties
│       └── db/migration/    # V1 – V5 (Flyway)
└── test/
    ├── controllers/         # 11 testes de integração (MockMvc)
    └── services/            # 9 testes unitários (Mockito)
```

### Frontend

```
frontend/src/
├── lib/
│   └── axios.ts                      # Instância Axios com interceptor JWT
├── contexts/
│   └── AuthContext.tsx               # Estado global de autenticação
├── router/
│   └── index.tsx                     # Rotas protegidas por papel
├── shared/
│   ├── components/
│   │   ├── Layout.tsx                # Shell com sidebar + navbar
│   │   ├── Sidebar.tsx               # Navegação filtrada por papel
│   │   ├── Navbar.tsx                # AppBar com usuário e papel
│   │   └── ProtectedRoute.tsx        # Guard de autenticação e autorização
│   └── utils/
│       └── formatters.ts             # formatCurrency, formatDate, formatCPF...
└── modules/                          # Um módulo por entidade de negócio
    ├── auth/           (types · service · LoginPage · RegisterPage)
    ├── dashboard/      (AdminDashboard · EmployeeDashboard · CustomerDashboard)
    ├── categories/     (types · service · CategoryTable · CategoryForm · Page)
    ├── products/       (types · service · ProductTable · ProductForm · Page)
    ├── customers/      (types · service · CustomerTable · CustomerForm · Page)
    ├── employees/      (types · service · EmployeeTable · EmployeeForm · Page)
    ├── sales/          (types × 2 · services × 2 · SaleTable · SaleForm · Page)
    ├── users/          (types · service · UserTable · UserRegistrationForm · Page)
    └── roles/          (types · service · RoleTable · RoleForm · Page)
```

---

## Testes

O backend possui **316 testes automatizados** divididos em duas camadas:

```bash
cd backend
./mvnw test
```

### Testes de Integração — Controllers (160 testes)

Utilizam `@WebMvcTest` + `MockMvc` com segurança real e serviços mockados. Cobrem rotas, status HTTP, serialização JSON e regras de autorização.

| Classe de Teste | Testes |
|---|---|
| AuthControllerTest | 9 |
| CategoryControllerTest | 13 |
| CustomerControllerTest | 18 |
| EmployeeControllerTest | 18 |
| PersonControllerTest | 16 |
| ProductControllerTest | 17 |
| RoleControllerTest | 16 |
| SaleControllerTest | 16 |
| SaleProductControllerTest | 10 |
| UserControllerTest | 19 |
| UserRegistrationControllerTest | 8 |

### Testes Unitários — Services (155 testes)

Utilizam `@ExtendWith(MockitoExtension.class)` com repositórios mockados. Cobrem regras de negócio, soft delete, gestão de estoque e tratamento de exceções.

| Classe de Teste | Testes |
|---|---|
| CategoryServiceTest | 15 |
| CustomerServiceTest | 16 |
| EmployeeServiceTest | 20 |
| PersonServiceTest | 19 |
| ProductServiceTest | 26 |
| RoleServiceTest | 14 |
| SaleProductServiceTest | 11 |
| SaleServiceTest | 19 |
| UserServiceTest | 15 |

Os relatórios são gerados em `backend/target/surefire-reports/`.

---

## Licença

Uso interno.
