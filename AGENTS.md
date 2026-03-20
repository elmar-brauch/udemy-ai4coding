# AI Agent Guide for udemy-ai4coding

## Project Identity
Spring Boot 3.4.5 e-commerce REST API (Java 21, H2 in-memory DB, Maven). Educational project demonstrating REST patterns—NOT production code.

## Critical: Build Commands
**Maven wrapper (`mvnw`) is BROKEN**—always use system `mvn`:
```powershell
mvn clean compile  # 5s, Lombok warnings normal
mvn test           # 11s, Mockito warnings expected  
mvn spring-boot:run # Start server on :8080
```

## Architecture: Dual Persistence Pattern
The defining architectural decision—**TWO storage strategies coexist intentionally**:

### Customer Module (In-Memory)
- `CustomerController` uses `HashMap<UUID, Customer>` directly in controller
- No JPA, no repository, no persistence between restarts
- Example of stateless REST API pattern
- See: `src/main/java/de/bsi/ai4coding/customer/CustomerController.java`

### Product Module (JPA Persistence)
- `Product` entity with `@Embedded Price` and `@ElementCollection Map<String,String> attributes`
- `ProductRepository extends JpaRepository<Product, UUID>`
- H2 in-memory DB (data lost on restart, but persisted during runtime)
- See: `src/main/java/de/bsi/ai4coding/product/`

**When extending:** Choose pattern based on requirements. `purchase/` and `order/` directories are empty placeholders—ignore them.

## Code Conventions That Matter

### Entity Structure (Product as exemplar)
```java
@Entity @Data  // Lombok generates getters/setters/equals/hashCode
public class Product {
    @Id @GeneratedValue private UUID id;
    @Enumerated(EnumType.STRING) private Category category;
    @Embedded private Price price;  // Nested value object
    @ElementCollection private Map<String, String> attributes;  // JPA handles Map persistence
}
```
- **Always** use `@Enumerated(EnumType.STRING)` for enums (never ORDINAL)
- `@Embedded` for complex nested objects (`Price` is `@Embeddable`)
- `@ElementCollection` for Map/Collection fields needing JPA persistence

### Manual Validation Pattern (NO Bean Validation)
This project does **NOT** use `@Valid`/`@NotNull` annotations. Validation is manual via private methods:
```java
private boolean isProductValidForCreation(Product p) {
    return p.getName() != null && 
           p.getCategory() != null && 
           p.getPrice() != null && 
           isPriceValid(p.getPrice()) &&  // Nested validation
           p.getAttributes() != null && !p.getAttributes().isEmpty();
}
```
**Pattern:** Separate validator methods, validate nested objects recursively, throw `ResponseStatusException(HttpStatus.BAD_REQUEST, "msg")` on failure.

### Error Handling
```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
```
No custom exception classes—use Spring's `ResponseStatusException` everywhere.

### Logging (Mandatory)
Every controller method logs with SLF4J:
```java
logger.info("POST /products - Created new product with ID: {}", savedProduct.getId());
logger.error("GET /customers/{} - Customer with ID: {} not found", id, customerId);
```

## Testing Strategy

### Controller Tests (`@WebMvcTest`)
Standard pattern for REST endpoint testing:
```java
@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper jsonMapper;
    @MockitoBean private ProductRepository productRepository;  // Mock for JPA modules
    
    @Test
    void testCreateProduct() throws Exception {
        when(productRepository.save(any())).thenReturn(savedProduct);
        
        mockMvc.perform(post("/products")
                .contentType(APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())  // ProductController uses ResponseEntity
                .andExpect(jsonPath("$.id").exists())
                .andDo(print());  // Use for debugging test failures
    }
}
```

**Key patterns:**
- Customer tests: No mocking needed (HashMap storage in controller)
- Product tests: Mock `ProductRepository` with `@MockitoBean`
- Use `jsonMapper.writeValueAsString()` for request bodies
- Chain `.andExpect()` for multiple assertions
- Always add `.andDo(print())` when debugging

### Known Test Anti-Patterns
See `CustomerControllerBuggyTest.java`—demonstrates what NOT to do:
- ❌ Don't rely on HashMap iteration order (`customers.getFirst()` is non-deterministic)
- ❌ Don't use `@RepeatedTest` to mask flaky tests—fix the root cause
- ✅ Tests must pass 100% of the time

## Known Issues
1. `ProductController.updateProduct()` line 85—bug uses `||` instead of `&&` in validation
2. GET `/products` with filters not implemented (TODO at line 22)

## Configuration Essentials

### H2 Database (Development)
- Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb` (username: `sa`, no password)
- Schema auto-created from `@Entity` classes on startup
- Data cleared on server restart

### Lombok Exclusion (pom.xml requirement)
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```
**Critical:** Lombok must be excluded from JAR repackaging but included in compiler annotation processing.

## Quick Workflows

### Adding New REST Endpoint
1. Add method to controller with `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`
2. Add manual validation (private `isXxxValid()` method if needed)
3. Add logging (`logger.info/error`)
4. Write `@WebMvcTest` test in `src/test/.../ControllerTest.java`
5. Run `mvn test`

### Adding New JPA Entity
1. Create `@Entity @Data` class with `@GeneratedValue UUID id`
2. Create `interface XxxRepository extends JpaRepository<Entity, UUID>`
3. Inject with `@Autowired` in controller (no service layer exists)
4. Run `mvn clean compile test` to verify schema generation

### Fixing Bugs
1. Write failing test reproducing the bug
2. Fix code
3. Verify with `mvn test` (all tests must pass)
4. No CI/CD—manual verification only

## Environment Notes
- **Windows/PowerShell** environment—use `;` for command chaining, backslashes in paths
- **Java 21**—use modern syntax (var, records, pattern matching where appropriate)
- **No service layer**—controllers call repositories directly
- **No security/authentication**—demo API only
- **No CI/CD pipelines**—all testing via local `mvn test`

## API Testing
Postman collection available: `ai4coding.postman_collection` (project root)

---
*For detailed examples, see `.github/copilot-instructions.md`. This guide focuses on what you can't learn from single-file inspection.*

