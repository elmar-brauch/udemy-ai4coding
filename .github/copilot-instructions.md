# AI Instructions for udemy-ai4coding

## Project Overview
Spring Boot 3.4.5 demo e-commerce REST API (Java 21, H2 in-memory DB, Maven). Educational project demonstrating REST development and testing patterns.

## Critical Build/Test Knowledge

### Maven Wrapper is BROKEN - Always Use System Maven
**NEVER use `mvnw` or `mvnw.cmd`** - they fail with proxy errors. Always use `mvn` directly.

### Standard Build Workflow (Windows/PowerShell)
```powershell
mvn clean          # Clear compiled artifacts (~1s)
mvn compile        # Compile sources (~5s, Lombok warnings are normal)
mvn test           # Run tests (~11s, Mockito warnings expected)
mvn package        # Full build with JAR (~10s)
mvn spring-boot:run # Start on http://localhost:8080
```

### Expected Warnings (Safe to Ignore)
- Lombok annotation processing warnings during `mvn compile`
- Mockito self-attaching warnings during `mvn test`

## Architecture Pattern: Dual Persistence Strategy

**Key architectural decision:** This project intentionally uses TWO different storage approaches:

1. **Customer module** → In-memory `HashMap` storage (no JPA, no database)
   - Simple CRUD with `Map<UUID, Customer>` in [CustomerController.java](../src/main/java/de/bsi/ai4coding/customer/CustomerController.java)
   - Data lost on server restart
   - Example of non-persistent REST API

2. **Product module** → JPA persistence with H2 database
   - [Product.java](../src/main/java/de/bsi/ai4coding/product/model/Product.java) uses `@Entity`, `@Embedded` (Price), `@ElementCollection` (attributes Map)
   - [ProductRepository.java](../src/main/java/de/bsi/ai4coding/product/repository/ProductRepository.java) extends `JpaRepository<Product, UUID>`
   - Standard Spring Data JPA pattern

**When adding new features:** Decide which pattern to follow based on requirements (in-memory vs. persistent).

## Module Structure & Extension Points

```
src/main/java/de/bsi/ai4coding/
├── customer/          # In-memory CRUD (complete)
├── product/           # JPA-based CRUD (mostly complete)
│   ├── controller/    # ProductController with known TODOs
│   ├── model/         # Product (entity) + Price (embeddable)
│   ├── repository/    # JpaRepository interface
│   └── util/          # Empty - utility classes go here
├── purchase/          # Empty placeholder directories - ignore
└── order/             # Empty placeholder directories - ignore
```

**purchase/ and order/ are empty placeholder directories** with no planned functionality - ignore them.

## Coding Conventions (Project-Specific)

### Entity Classes
- Use Lombok: `@Data` for getters/setters, `@Entity` for JPA entities, `@AllArgsConstructor`/`@NoArgsConstructor` for constructors
- UUID primary keys with `@GeneratedValue`
- Enums for fixed values: `@Enumerated(EnumType.STRING)` (see `Product.Category`, `Price.Currency`)
- Complex nested objects use `@Embedded` (see `Product.price` field containing `Price` embeddable)
- Map fields use `@ElementCollection` (see `Product.attributes` for String-to-String mapping)

### Controllers
- `@RestController` + `@RequestMapping("/resource-path")`
- Return entities directly (Spring serializes to JSON automatically)
- Use `ResponseEntity<T>` when custom HTTP status codes needed (e.g., 201 Created in `createProduct()`)
- Error handling: `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message")`
- Logging: Manual `Logger` for all operations (`logger.info("GET /customers/{} - Retrieving...", id)`)

### Validation Pattern (ProductController)
See [ProductController.java](../src/main/java/de/bsi/ai4coding/product/controller/ProductController.java) for validation helpers:
- Separate private methods like `isProductValidForCreation()` and `isPriceValid()`
- Check all required fields are non-null before persistence
- Nested validation: validate embedded objects separately (Price validation within Product validation)
- Throw `ResponseStatusException` with BAD_REQUEST for validation failures

### Testing Patterns

#### Controller Tests (`@WebMvcTest`)
Use for testing REST endpoints without starting full application. See [CustomerControllerTest.java](../src/test/java/de/bsi/ai4coding/customer/CustomerControllerTest.java):
```java
@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper jsonMapper;
    
    @Test
    void testCreateCustomer() throws Exception {
        Customer newCustomer = new Customer(null, "John Doe", 30);
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newCustomer)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"))
                .andDo(print());
    }
}
```

Key patterns:
- Use `jsonMapper.writeValueAsString()` to serialize request bodies
- Use `jsonMapper.readValue()` with `TypeReference<List<T>>` for deserializing list responses
- Use `.andDo(print())` for debugging test failures
- Chain `.andExpect()` calls for multiple assertions

#### Repository Tests (Future)
When adding JPA repository tests:
- Use `@DataJpaTest` for testing repository layer only
- Use `TestEntityManager` for setting up test data
- Test custom query methods, not basic CRUD (Spring Data already tests those)
- Example structure:
```java
@DataJpaTest
class ProductRepositoryTest {
    @Autowired private ProductRepository repository;
    @Autowired private TestEntityManager entityManager;
}
```

#### Service Tests (Future)
When adding service layer:
- Use `@SpringBootTest` for integration tests or plain JUnit with `@Mock`/`@InjectMocks`
- Mock repository calls with Mockito
- Test business logic, not framework behavior

#### Test Anti-Patterns
See [CustomerControllerBuggyTest.java](../src/test/java/de/bsi/ai4coding/customer/CustomerControllerBuggyTest.java) for what NOT to do:
- **Don't rely on HashMap iteration order** - `customers.getFirst()` may return different results
- **Don't use `@RepeatedTest` to mask flaky tests** - fix non-determinism instead
- Tests must pass consistently 100% of the time

## Known Issues & TODOs

1. **[ProductController.java:22](../src/main/java/de/bsi/ai4coding/product/controller/ProductController.java#L22)** - Missing GET all products endpoint (needs filters)
2. **[ProductController.java:68](../src/main/java/de/bsi/ai4coding/product/controller/ProductController.java#L68)** - Bug: `updateProduct()` uses OR (`||`) instead of AND (`&&`) in validation

## Configuration & Runtime

### Database (H2 Console)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb` (from [application.properties](../src/main/resources/application.properties))
- Username: `sa`, Password: *(empty)*
- Data is in-memory only - lost on server restart

### API Endpoints
**Customer API** (in-memory storage):
- `GET /customers` - List all customers
- `POST /customers` - Create customer (JSON body: `{name, age}`)
- `GET /customers/{id}` - Get customer by UUID
- `PUT /customers/{id}` - Update customer
- `DELETE /customers` - Delete all customers

**Product API** (JPA/H2 storage):
- `POST /products` - Create product (validates all fields including nested Price)
- `GET /products/{id}` - Get product by UUID
- `PUT /products/{id}` - Update product (has bug)
- `DELETE /products/{id}` - Delete product

### Running the Application
```powershell
mvn spring-boot:run   # From Maven
java -jar target\udemy-ai4coding-0.1.jar  # From JAR (after mvn package)
```

Both start server on http://localhost:8080. Use Postman collection (`ai4coding.postman_collection`) for testing.

## Quick Reference Workflows

### Adding New REST Endpoint
1. Add method to controller with `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`
2. Write test in corresponding `src/test/.../ControllerTest.java` using `@WebMvcTest` pattern
3. Run `mvn test` to verify
4. Example commit: Add endpoint + test in same commit

### Adding New JPA Entity
1. Create `@Entity @Data` class in `model/` package with `@GeneratedValue UUID id`
2. Create repository: `interface XxxRepository extends JpaRepository<Entity, UUID> {}`
3. Inject repository into controller with `@Autowired`
4. Run `mvn clean compile test` to verify JPA schema generation

### Fixing a Bug
1. Write a failing test that reproduces the bug (e.g., test PUT with null fields should return 400)
2. Fix the code (e.g., change `||` to `&&`)
3. Run `mvn test` to verify fix
4. Run `mvn package` to ensure full build succeeds

### Validation Before Committing
Run in sequence:
```powershell
mvn clean
mvn compile  # Check for compilation errors
mvn test     # All tests must pass
```

## Important Notes for AI Agents

- **This is a Windows environment** - use PowerShell syntax, backslashes in paths
- **Trust these instructions** - commands and timings are from actual execution
- The project uses **Java 21 features** - use modern syntax (var, records, pattern matching)
- **No service layer exists yet** - controllers call repositories directly
- **No security/authentication** - this is a demo API
- **No input validation annotations** (`@Valid`, `@NotNull`) - validation is manual in controller methods
- Customer API **data is lost on restart** (HashMap storage)
- Product API **data persists in H2** until server restart (in-memory DB)
- Lombok **must be excluded** from Spring Boot Maven plugin repackaging (see [pom.xml](../pom.xml))
- **No CI/CD pipelines** - all testing is manual via `mvn test`
