# Spring Common Mistakes Workshop

**Welcome to the Spring Common Mistakes Workshop!**

In this workshop, you'll gain insights into some of the most frequent mistakes developers make while working with Spring 
applications—and, more importantly, how to avoid them. We will cover a range of topics, from architectural decisions and 
logging best practices to data access patterns, API design, performance tuning, testing, and security considerations.

By the end of this workshop, you'll have a deeper understanding of how to structure your Spring applications effectively, 
write cleaner and more secure code, and make better decisions about performance and architecture.

### What You Will Learn:
- How to organize code effectively and follow dependency injection principles.
- Best practices for logging, data access, and API design.
- Common pitfalls in transaction management, configuration, and testing.
- Strategies for securing your Spring applications and optimizing performance.

### Prerequisites:
- Familiarity with Spring Boot and basic Java programming.
- A general understanding of web development concepts, databases, and Web APIs.

Get ready to dive deep into the Spring ecosystem and level up your skills by learning from common mistakes—let's get started!

## Agenda 

- **[Architecture](#architecture)** 
  - [Default package](#default-package)
  - [Component Scanning](#component-scanning-)
  - [Organizing Code](#organizing-code)
    - Package By Layer
    - Package by Feature
  - [Spring Modulith](#spring-modulith) 🔥
  - [Ignoring Dependency Injection Principles](#ignoring-dependency-injection-principles)
- **[Logging](#logging)**
  - [Using System.out.println Instead of a Logging Framework](#using-systemoutprintln-instead-of-a-logging-framework)
  - [Logging Sensitive Information](#logging-sensitive-information)
- **[Data Access](#data-access)**
  - [Shifting Left (Docker Compose)](#shifting-left-docker-compose)
  - [Picking the right abstraction layer](#picking-the-right-abstraction-layer)
    - JDBC Template / JDBC Client
    - Spring Data JPA
    - Spring Data JDBC
  - [N+1 Query Problem](#n1-query-problem)
  - [Neglecting transaction management](#neglecting-transaction-management)
    - Incorrect Use of @Transactional
    - Not Understanding Transaction Propagation
    - Transactions on Non-Public Methods
- **[Web APIs](#web-apis)**
  - [REST](#rest)
    - Improper RESTful API Design
    - Lack of Versioning
    - Controller Bloat
    - Poor Exception Handling
    - Lack of API Documentation
  - [GraphQL](#graphql)
- **[Configuration](#configuration)** 
  - [Hardcoding Configuration Values](#hardcoding-configuration-values)
  - [Not Using Environment Variables](#not-using-environment-variables)
- **[Performance](#performance)** 
  - [Virtual Threads](#virtual-threads)
  - [Native Images](#native-images)
- **[Testing](#testing)**
  - Overusing @SpringBootTest
  - Insufficient Unit Testing
- **[Security](#security)**
  - Not Adopting Secure by Default ("Fail Closed")
  - Implementing your own JwtFilter
- **[Resources](#resources)**

## Architecture

### Default package

Putting code in the default package (no package declaration) causes several issues:

- Component scanning may not work properly, affecting autowiring and dependency injection
- Makes it impossible to use package-private visibility for encapsulation
- Creates tight coupling between classes
- Makes the codebase harder to maintain and test
- Violates separation of concerns principle

```java
@Component
public class WelcomeMessage {

    public String getWelcomeMessage() {
        return "Welcome to ThoughtStream!";
    }

}
```

### Component Scanning 

When classes are in the same package or sub-packages as the main application class annotated with 
`@SpringBootApplication`, Spring Boot automatically scans and registers them as beans through its component scanning 
mechanism. The @SpringBootApplication annotation includes `@ComponentScan` which by default scans the package 
containing the main class and all its sub-packages.


```java
@SpringBootApplication
@ComponentScan(basePackages = "dev.danvega.base")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

### Organizing Code

Package by Feature benefits:

- Better encapsulation: Only controllers need to be public, internal components can be package-private
- Higher cohesion: Related code stays together
- Reduces coupling: Features are isolated, changes are contained
- Clear boundaries: Each feature package is a mini bounded context
- Easier testing: Feature components can be tested together
- Better modularity: Features can be moved or modified as units
- Clear dependencies: Inter-feature communication must be explicit through public APIs

❌ Package by Layer (Traditional)
- com.example.demo
- .controller
  - PostController.java      // public
  - CommentController.java   // public
- .service
  - PostService.java        // public
  - CommentService.java     // public
- .repository
  - PostRepository.java     // public
  - CommentRepository.java  // public
- .model
  - Post.java              // public
  - Comment.java           // public
  - Reaction.java          // public
  - Tag.java               // public

✅ Package by Feature
- com.example.demo
- .post
  - PostController.java      // public
  - PostService.java        // package-private
  - PostRepository.java     // package-private
  - Post.java              // package-private
  - Tag.java               // package-private
- .comment
  - CommentController.java   // public
  - CommentService.java     // package-private
  - CommentRepository.java  // package-private
  - Comment.java           // package-private
  - Reaction.java          // package-private

- Overuse of public access modifier
  - Improper Layering (Package by Feature, not by Layer)
  - Spring Modulith 🔥

### Spring Modulith

Spring Modulith helps structure large Spring Boot applications into well-defined, loosely coupled modules. It provides:

- Module boundaries through package conventions
- Runtime validation of module dependencies
- Documentation generation
- Event-driven communication between modules

**Structure Example**

```java
com.example.application
  .post     // A module
  .comment // Another module  
  .user  // Another module
```

#### Key Benefits
- Clear module boundaries
- Dependency validation
- Better maintainability
- Architectural enforcement
- Module documentation

#### Spring Modulith Resources

1. [Official Documentation](https://spring.io/projects/spring-modulith)
2. [Introduction Blog Post](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
3. [Getting Started Guide](https://docs.spring.io/spring-modulith/docs/current/reference/html/#getting-started)
4. [Sample Applications](https://github.com/spring-projects/spring-modulith/tree/main/spring-modulith-examples)

### Ignoring Dependency Injection Principles

Key points about dependency injection in Spring Boot:

1. Field injection (@Autowired) hides dependencies, prevents immutability, complicates testing, and enables circular dependencies.
2. Constructor injection:
   - Makes dependencies explicit
   - Enables final fields
   - Simplifies testing
   - Prevents circular dependencies
   - Forces better design decisions
3. Common signs of poor DI practices:
   - Multiple @Autowired fields
   - Non-final dependencies
   - Classes requiring Spring context for testing
   - Circular dependencies between services
4. Recommendations:
   - Use constructor injection exclusively
   - Keep dependencies minimal
   - Mark fields as final
   - Consider @RequiredArgsConstructor from Lombok
   - Use @Configuration for complex scenarios

```java
// ❌ Circular Dependency Example
@Service
public class UserService {
    @Autowired
    private AuthService authService;

    public User createUser(UserDto dto) {
        authService.validateUserCreation(dto);
        return saveUser(dto);
    }
}

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    public void validateUserCreation(UserDto dto) {
        if (userService.exists(dto.getEmail())) {
            throw new UserAlreadyExistsException();
        }
    }
}

// ✅ Fixed Version - Extract Common Logic
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public User createUser(UserDto dto) {
        authService.validateUserCreation(dto);
        return saveUser(dto);
    }
}

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUserCreation(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException();
        }
    }
}

// The fix involves:
// 1. Identifying the shared functionality (user lookup)
// 2. Moving it to a common dependency (UserRepository)
// 3. Using constructor injection to make dependencies explicit
// 4. Each service now depends on the repository, not each other
```

## Logging

### Using System.out.println Instead of a Logging Framework

**Problems with System.out.println()**

1. No Log Levels
   - `System.out.println()` offers no severity levels (DEBUG, INFO, ERROR, etc.)
   - Cannot filter logs based on importance or environment
   ```java
    // No way to differentiate between debug and error messages
    System.out.println("User login failed"); // Is this an error? Warning?
    System.out.println("Database connection established"); // Is this info? Debug?
    ```
2. Performance Impact
   - I/O operations are synchronous and block the main thread
   - No buffering or async logging capabilities
   - No performance optimizations for high-throughput scenarios
   ```java
    // Each call blocks until the message is written
    for(User user : users) {
    System.out.println("Processing user: " + user.getId()); // Blocks on every iteration
    }
    ```
3. No Configuration Control
   - Can't disable logging in production
   - Can't redirect output to different destinations
   - No rotation or file management
   ```java
    // Hard to maintain and impossible to configure
    System.out.println("Debug: " + complexObject.toString());
    ```

#### Benefits of Proper Logging Frameworks (SLF4J + Logback)

1.Structured Logging
2. Configuration Flexibility
3. Performance Optimizations
   * Lazy evaluation of log messages
   * Asynchronous logging
   * Log level filtering at compile-time
4. Production-Ready Features
   * Log rotation and archiving
   * Multiple output destinations
   * Contextual information (thread, class, timestamp)
   * MDC (Mapped Diagnostic Context) for request tracing

#### Best Practices

1. Use SLF4J as the logging facade
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

2. Configure appropriate log levels
   * ERROR: Application failures requiring immediate attention
   * WARN: Unexpected situations that don't cause failure
   * INFO: Important business events
   * DEBUG: Detailed information for debugging
   * TRACE: Most detailed level for development


3. Include contextual information
```java
logger.error("Transaction {} failed= for user {}", transactionId, userId);
```

4. Use parameterized logging
```java
// Good
logger.debug("Processing order {}", orderId);

// Bad
logger.debug("Processing order " + orderId);  // Creates string regardless of log level
```

#### Logging Resources

[You might not need Lombok](https://www.danvega.dev/blog/no-lombok)

Live Template: 
```java
private static final Logger log = LoggerFactory.getLogger($CLASS_NAME$.class);
```

### Logging Sensitive Information

Logging sensitive information creates serious security and compliance risks. When logs contain data like credit card numbers, SSNs, or health information, they become attractive targets for attackers and can lead to identity theft and fraud. Unlike databases, logs often lack proper security controls, may be widely distributed, and can persist indefinitely. Exposing sensitive data in logs also violates regulations like GDPR and HIPAA, potentially resulting in hefty fines and mandatory breach notifications.

```java
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // WRONG: Logging sensitive information
    public void createUser(User user) {
        logger.info("Creating user with SSN: {}, credit card: {}", 
            user.getSsn(), 
            user.getCreditCard().getNumber()
        );
    }

    // WRONG: Exception logging exposing sensitive data
    public void processPayment(Payment payment) {
        try {
            paymentProcessor.process(payment);
        } catch (Exception e) {
            logger.error("Payment failed for card: " + payment.getCreditCardNumber(), e);
        }
    }

    // WRONG: Debug logging with PII
    public void updateProfile(UserProfile profile) {
        logger.debug("Updating profile: {}", profile.toString());  // toString() might include all fields
    }
}
```

#### Best Practices

1. **Mask Sensitive Data**
```java
public class SensitiveDataMasker {
    public static String maskCreditCard(String ccNumber) {
        if (ccNumber == null) return null;
        return "****-" + ccNumber.substring(ccNumber.length() - 4);
    }

    public static String maskSSN(String ssn) {
        if (ssn == null) return null;
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}

@Service
public class UserService {
    public void createUser(User user) {
        logger.info("Creating user with masked SSN: {}", 
            SensitiveDataMasker.maskSSN(user.getSsn())
        );
    }
}
```

2. **Custom toString() Methods**
```java
@Entity
public class User {
    private String ssn;
    private String creditCardNumber;
    private String email;

    @Override
    public String toString() {
        return String.format("User[id=%d, email=%s]", 
            id, 
            maskEmail(email)
        );
    }
}
```

3. **Use MDC for Tracking Without PII**
```java
public class SecureUserContext {
    public void processUserRequest(String userId) {
        String sessionId = generateSessionId();  // Random or hash-based ID
        MDC.put("sessionId", sessionId);
        MDC.put("userId", hashUserId(userId));  // Store hashed version
        
        try {
            // Processing
        } finally {
            MDC.clear();
        }
    }
}
```

MDC (Mapped Diagnostic Context) is a logging utility concept primarily used in Java applications, though similar patterns exist in other languages. It's essentially a thread-local storage mechanism that allows you to attach contextual information to log messages throughout the execution of a request or operation.

4. **Configure Logging Patterns**
```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <encoder>
        <!-- Avoid including full stack traces in logs -->
        <pattern>%date [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

5. **Implement Log Sanitization**
```java
public class SecureLogger {
    private static final Pattern CREDIT_CARD = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
    private static final Pattern SSN = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");

    public static String sanitize(String message) {
        if (message == null) return null;
        
        message = CREDIT_CARD.matcher(message).replaceAll("****-****-****-****");
        message = SSN.matcher(message).replaceAll("***-**-****");
        return message;
    }
}

// Usage
logger.info("Processing: {}", SecureLogger.sanitize(message));
```

#### Sensitive Data Checklist

Never log:
- Social Security Numbers
- Credit Card Numbers
- Passwords or Security Questions
- Access Tokens/API Keys
- Bank Account Details
- Health Information (HIPAA)
- Full Names with Other PII
- Biometric Data
- Exact Geolocation Data
- Children's Information

#### Regulatory Compliance

- GDPR: Requires protection of personal data
- HIPAA: Strict requirements for health information
- PCI DSS: Credit card data protection
- CCPA: California privacy requirements

Each has specific requirements about logging and data protection.

## Data Access

### Shifting Left (Docker Compose)

One of the most common pitfalls in Spring Boot development is using H2 or other in-memory databases during development while running a different database (like PostgreSQL or MySQL) in production. While H2 is convenient and requires minimal setup, this practice creates a dangerous disconnect between development and production environments.

#### The Problem

When developers use H2 in development but PostgreSQL in production, they're essentially testing their application against a completely different database engine. This can lead to several issues:

1. Database-specific SQL features that work in H2 might fail in PostgreSQL
2. Different transaction isolation behaviors between databases
3. Missed performance issues that only appear with production database
4. Schema migration scripts that work in H2 but fail in PostgreSQL
5. Inconsistent handling of case sensitivity and data types

#### The Solution: Docker Compose + Spring Boot

Spring Boot 3.1+ includes built-in Docker Compose support, making it trivially easy to spin up production-like databases during development. Instead of reaching for H2, developers can:

- Use the same database engine as production
- Match production database versions exactly
- Test against real database behavior
- Validate migrations against the actual target database
- Catch database-specific issues early in development

This approach follows the "dev/prod parity" principle from the Twelve-Factor App methodology, reducing the likelihood of environment-specific bugs and making the development environment more reliable.

### Picking the right abstraction layer

One of the most impactful architectural decisions in a Spring Boot application is selecting the appropriate data access abstraction level. Each abstraction offers different trade-offs between productivity, performance, and complexity. Let's explore when to use each option.

#### JdbcTemplate / JdbcClient (Low-Level Abstraction)

**Best For:**
- Performance-critical applications
- Simple CRUD operations with complex queries
- When you need complete SQL control
- Batch operations
- Legacy database integration

**Trade-offs:**
- Maximum control over SQL and performance
- Direct mapping between SQL and code
- No lazy loading or caching
- More boilerplate code
- Manual mapping between results and objects

#### Spring Data JDBC (Middle-Ground Abstraction)

**Best For:**
- Domain-Driven Design (DDD) applications
- When you want JPA-like convenience without the complexity
- Applications requiring predictable database access patterns
- Teams that want to maintain control over database interactions

**Trade-offs:**
- No lazy loading (which can be a feature!)
- Explicit aggregate boundaries
- Simpler persistence model than JPA
- Better performance than JPA for most use cases
- Less "magic" than JPA

#### Spring Data JPA (High-Level Abstraction)

**Best For:**
- Rapid application development
- Complex domain models with many relationships
- When database portability is important
- Teams familiar with JPA/Hibernate concepts
- Applications where query performance isn't the primary concern

**Trade-offs:**
- Highest productivity for complex domain models
- Powerful querying capabilities (JPQL, Criteria API)
- Cache management and lazy loading
- Can hide performance issues
- Steeper learning curve for proper usage

#### Decision Framework

Consider these factors when choosing:
1. **Team Experience**: Does your team understand JPA's pitfalls and benefits?
2. **Performance Requirements**: Do you need fine-grained control over SQL?
3. **Domain Complexity**: How complex are your entity relationships?
4. **Maintainability**: Who will maintain this code in the future?
5. **Time Constraints**: Do you need rapid development or long-term optimization?

**Remember:** You can mix these abstractions in the same application. Use `JdbcClient` for performance-critical queries while using JPA for simpler CRUD operations.

### N+1 Query Problem

The N+1 query problem is one of the most common performance anti-patterns in database access. Here's why it matters:

Problem: When loading a parent entity with child collections, developers make:

One query to fetch parent records
N separate queries to fetch child records (one per parent)

Example scenario with our posts:

```sql
-- Initial query
SELECT * FROM posts;  -- Gets 100 posts

-- Then for EACH post (100 times):
SELECT * FROM comments WHERE post_id = ?
```

**Impact:**

- Network latency multiplies (100ms × 100 queries = 10s delay)
- Database connection pool saturates
- Performance degrades exponentially with data growth

**Better approach:**

```sql
SELECT p.*, c.*
FROM posts p 
LEFT JOIN comments c ON p.id = c.post_id;
```

Common places to look:

* One-to-many relationships (Post -> Comments)
* Many-to-many relationships (Post -> Tags)
* Lazy loading in ORMs
* REST endpoints returning nested resources

Prevention:
* Use JOIN queries
* Implement batch fetching
* Use database profiling tools
* Review ORM fetch strategies carefully
* Consider GraphQL for flexible data loading

This problem becomes especially critical in microservices where network latency compounds the issue.

### Neglecting transaction management

Transactions ensure database operations are atomic, consistent, isolated, and durable (ACID):

```java
@Service
public class PostService {

    // Without transaction - potential inconsistency
    public void createPostWithTags(Post post, List<Tag> tags) {
        postRepository.save(post);     // If this succeeds but...
        tagRepository.saveTags(tags);  // ...this fails, data is inconsistent
    }

    // With transaction - all or nothing
    @Transactional
    public void createPostWithTags(Post post, List<Tag> tags) {
        postRepository.save(post);     
        tagRepository.saveTags(tags);  // If this fails, post save is rolled back
    }
}
```

**Key benefits:**
* Atomicity: Multiple operations succeed or fail together
* Consistency: Database moves from one valid state to another
* Isolation: Concurrent transactions don't interfere
* Durability: Committed changes persist

**Common scenarios requiring transactions:**
* Financial operations
* Multi-table updates
* User registration flows
* Order processing
* Data migrations

Without transactions, system crashes or concurrent access can leave data in an invalid state.

**Key issues:**
* Proxies and self-invocation - transactions only work through proxy calls
* Public method requirement - Spring AOP needs public methods
* Default REQUIRED vs REQUIRES_NEW - understand implications on transaction nesting
* Exception handling impacts rollback behavior
* Read operations need transaction for consistency
* Transaction boundaries should align with business operations

**Examples:** 

`PostService` & `CommentService` both use transactions:

## Web APIs

### REST

1. Improper RESTful API Design
   * Using verbs instead of nouns in endpoints (/createUser vs /users)
   * Inconsistent response structures across endpoints
   * Not using appropriate HTTP methods (POST for reads, GET for updates)
   * Not using proper HTTP status codes
   * Mixing plural and singular resource names
    ```java
    // Bad
    @PostMapping("/getAllUsers")
    public List<User> getUsers() { ... }
    
    // Good
    @GetMapping("/users")
    public List<User> getUsers() { ... }
    ```

2. Lack of Versioning
    * Not planning for API evolution
    * Breaking changes without version control
    ```java
    - URI versioning: /api/v1/users
    - Header versioning: Accept: application/vnd.company.app-v1+json
    - Parameter versioning: /api/users?version=1
    - Media type versioning: Accept: application/json;version=1
    ```
3. Controller Bloat
   * Business logic in controllers
   * Complex error handling
   * Validation logic mixed with request handling
   ```java
    // Bad
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
    if (!isValidEmail(request.getEmail())) {
    throw new BadRequestException("Invalid email");
    }
    // Business logic here
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    return ResponseEntity.ok(userRepository.save(user));
    }
    
    // Good
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest request) {
    User user = userService.createUser(request);
    return ResponseEntity.ok(user);
    }
    ```
4. Poor Exception Handling
   * Generic error messages
   * Inconsistent error response structure
   * Not using global exception handlers   
   ```java
    @ControllerAdvice
    public class GlobalExceptionHandler {
   
        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
            ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
            );
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
   
    }
    ```
   
5. Lack of API Documentation
   * Missing or outdated documentation
   * Not using OpenAPI/Swagger
   * Incomplete example requests/responses
   ```java
    @Operation(summary = "Create new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {}
    ```

### GraphQL

GraphQL is a query language and runtime for APIs that allows clients to request specific data structures and receive exactly that data, replacing the need for multiple REST endpoints with a single flexible endpoint.

GraphQL addresses several key API development challenges:

* Over/Under-fetching: Clients specify exactly what data they need, eliminating unnecessary data transfer and reducing API calls
* Multiple Resource Fetching: Single request can retrieve data from multiple resources, replacing multiple REST endpoints
* Type Safety: Strong type system ensures data consistency and provides better development-time validation
* Schema Evolution: Enables adding fields without breaking existing queries, making API versioning more manageable
* Real-time Updates: Built-in subscription support for live data updates
* Documentation: Self-documenting through introspection, schemas automatically generate documentation
* Frontend Development Speed: Flexible data fetching reduces frontend-backend coordination and API endpoint negotiations

Spring for GraphQL integrates GraphQL into Spring applications, solving several key problems:

* Schema-First Development: Enables defining GraphQL schemas separately from code, improving API design and documentation
* Integration with Spring ecosystem: Seamlessly connects GraphQL operations with Spring components, controllers, and security features
* DataFetcher Mapping: Automatically maps GraphQL fields to corresponding Spring beans and methods, reducing boilerplate code
* Subscription Support: Built-in handling of real-time updates through WebSocket integration
* Error Handling: Standardized exception handling and error responses consistent with Spring's error management

http://localhost:8080/graphiql

```graphql
{
  posts {
    id
    content
    commentCount
    author {
      username
    }
    createdAt
    draft
    reactionCount
    tags {
      name
    }
    comments {
      content
      createdAt
      reactions {
        type
      }
    }
  }
}
```

#### GraphQL Resources

- [GraphQL](https://graphql.org/)
- [Spring for GraphQL Documentation](https://docs.spring.io/spring-graphql/reference/index.html)
- [My YouTube GraphQL Playlist](https://www.youtube.com/playlist?list=PLZV0a2jwt22slmUC9iwGGWfRQRIhs1ELa)

## Configuration

### Hardcoding Configuration Values

Spring Boot applications commonly struggle with hardcoded configuration values and environment-specific settings.

```java
@Repository
public class PostRepository {
    // ❌ Hardcoded configuration values
    private static final int MAX_SEARCH_RESULTS = 100;
    private static final String DEFAULT_SORT_ORDER = "DESC";
    
    public List<Post> search(String keyword) {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL +
            " WHERE p.content ILIKE :keyword " + 
            "ORDER BY p.created_at " + DEFAULT_SORT_ORDER + 
            " LIMIT " + MAX_SEARCH_RESULTS)
            .param("keyword", "%" + keyword + "%")
            .query(Post.class)
            .list();
    }
}
```

#### 1. Externalize Configuration in application.yaml

```yaml
spring:
  application:
    name: scm-workshop
  sql:
    init:
      mode: always
    
app:
  post:
    search:
      max-results: 100
      default-sort: DESC
  feeds:
    cache-ttl: 3600
    batch-size: 50
```

#### 2. Create Configuration Properties Class

```java
@ConfigurationProperties(prefix = "app.post.search")
@Component
record PostSearchProperties(
    @DefaultValue("100") Integer maxResults,
    @DefaultValue("DESC") String defaultSort
) {}
```

Creating a configuration properties class provides several key benefits beyond just IDE intellisense:

* Type Safety: Validates configuration at startup, catching misconfigurations early
* Strong Typing: Converting string values to proper types (Integer, Boolean, etc.) automatically
* Default Values: Built-in support for fallback values using @DefaultValue
* Documentation: IDE tooltips and completion for available properties, making configuration more discoverable
* Validation: Can add validation annotations (@Min, @Max, etc.) to enforce constraints
* Modular Config: Groups related properties together logically vs scattered @Value annotations
* Testing: Easier to create test configurations with builder patterns
* Refactoring: IDE support for renaming properties across codebase

#### 3. Use Configuration in Code

```java
@Repository
public class PostRepository {
    private final PostSearchProperties searchProperties;
    
    public PostRepository(JdbcClient jdbcClient, 
                         ObjectMapper objectMapper,
                         PostSearchProperties searchProperties) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.searchProperties = searchProperties;
    }
    
    public List<Post> search(String keyword) {
        return jdbcClient.sql(POST_WITH_RELATIONS_SQL +
            " WHERE p.content ILIKE :keyword GROUP BY p.id, u.id " + 
            "ORDER BY p.created_at " + searchProperties.defaultSort() + 
            " LIMIT " + searchProperties.maxResults())
            .param("keyword", "%" + keyword + "%")
            .query(Post.class)
            .list();
    }
}
```

**Benefits**

1. **Environment-Specific Values**: Different configurations for dev/staging/prod
2. **Version Control**: Track configuration changes in source control
3. **Validation**: Type-safe configuration with validation at startup
4. **Flexibility**: Override via environment variables or command line
5. **Testing**: Easy configuration overrides for testing scenarios

**Best Practices:**

1. Use meaningful property names and hierarchical structure
2. Group related properties using nested objects
3. Provide sensible defaults using `@DefaultValue`
4. Validate configuration values at startup
5. Use profiles for environment-specific values
6. Document configuration properties
7. Avoid sensitive data in configuration files

**Environment-Specific Configuration**

```yaml
# application-dev.yaml
app:
  post:
    search:
      max-results: 50
      default-sort: DESC

# application-prod.yaml
app:
  post:
    search:
      max-results: 100
      default-sort: DESC
```

Activate profiles using:
- `spring.profiles.active` property
- Environment variable: `SPRING_PROFILES_ACTIVE=prod`
- Command line: `--spring.profiles.active=prod`

### Environment Variables 

An environment variable is a dynamic value stored at the operating system level that can be used by applications to configure 
their behavior, with common examples being database credentials, API keys, or service URLs that may change between 
different environments (development, staging, production).

```java
# ❌ Hardcoded sensitive values in application.yaml
spring:
  datasource:
    url: jdbc:postgresql://production-db.thoughtstream.com:5432/thoughtstream
    username: admin
    password: supersecret123
  
security:
  jwt:
    secret: my-ultra-secure-and-ultra-long-secret-key
    expiration: 86400000
```

```java
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
```

Here's how to set environment variables in both operating systems:

Windows (Command Prompt):
```shell
set DATABASE_URL=postgresql://localhost:5432/db
```

macOS/Linux (Terminal):
```shell
export DATABASE_URL=postgresql://localhost:5432/db
```

To make these permanent:
* Windows: Set through System Properties > Environment Variables
* macOS: Add to ~/.zshrc or ~/.bash_profile

You can also set this in IntelliJ by going to Run > Edit Configurations > Environment Variables.

## Performance

### Virtual Threads

Virtual Threads, introduced in Java 21, solve a fundamental problem in server applications: the limited number of platform threads for handling concurrent operations.

#### The Problem

Traditional server applications use platform threads (OS threads) to handle requests. When a request performs blocking I/O (database queries, HTTP calls, file operations), the thread is blocked and unavailable for other work. This leads to:

* Limited concurrent requests (typical max: few hundred threads)
* Wasted resources during I/O operations
* Reduced application scalability
* Need for complex async programming

#### Virtual Threads Solution

Virtual Threads are lightweight threads that don't map 1:1 with OS threads. When a Virtual Thread performs blocking I/O:

* It's unmounted from the platform thread
* Stored in heap memory
* Remounted when I/O completes

This allows:

* Millions of concurrent Virtual Threads (The illusion of infinite scalability)
* Efficient use of system resources
* Simple synchronous programming model
* Better scalability for I/O-bound applications

#### Enabling Virtual Threads in Spring Boot 3.2+

Add to application.properties / application.yaml:

```properties
spring.threads.virtual.enabled=true
```

#### Example

```java
@RestController
@RequestMapping("/api")
public class ExampleController {

    private final RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(ExampleController.class);

    public ExampleController(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.example.com").build();
    }

    @GetMapping("/data")
    public String getData() {
        // Blocking operation - ideal for Virtual Threads
        ResponseEntity<String> response = restClient.get()
            .uri("/some-endpoint")
            .retrieve()
            .toEntity(String.class);
            
        log.info("Executed on thread: {}", Thread.currentThread());
        return response.getBody();
    }
}
```

Virtual threads in Spring Boot 3.2+ are enabled in the following components:

1. Tomcat (Web Server)
   * Handles incoming HTTP requests using virtual threads
   * Replaces the default platform thread pool

2. @Async methods
   * When using @EnableAsync
   * Default executor uses virtual threads

3. @Scheduled tasks
   * When using @EnableScheduling
   * Task executor uses virtual threads

4. WebClient & RestClient
   * HTTP client operations use virtual threads


What about when you aren't in one of those components? You can create your own virtual thread executor:

```java
@Component
public class VirtualThreadDemo implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(VirtualThreadDemo.class);
    private final RestClient restClient;

    public VirtualThreadDemo(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://httpbin.org").build();
    }

    @Override
    public void run(String... args) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Submit multiple tasks
            List<Future<String>> futures = IntStream.range(0, 10)
                .mapToObj(i -> executor.submit(() -> makeBlockingCall(i)))
                .toList();

            // Wait for all tasks to complete
            for (Future<String> future : futures) {
                log.info(future.get());
            }
        }
    }

    private String makeBlockingCall(int i) {
        ResponseEntity<Void> response = restClient.get()
            .uri("/delay/2")
            .retrieve()
            .toBodilessEntity();
            
        return "Task %d completed on %s".formatted(i, Thread.currentThread());
    }
}
```

### Native Images

GraalVM Native Images convert Java applications into standalone native executables, offering:

* Fast startup time (milliseconds vs seconds)
* Lower memory usage
* Reduced docker image size
* Improved serverless performance

Add to your pom.xml: 

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

```shell
# build
./mvnw -Pnative native:compile -DskipTests

# Cloud Native Buildpacks
./mvnw spring-boot:build-image
```

GraalVM native images operate on a "closed world assumption" - all code must be known at build time. Any runtime dynamic behavior like reflection, resource loading, or proxy creation requires explicit configuration through GraalVM hints. This fundamentally changes how you architect Spring applications, as common patterns like classpath scanning and dynamic bean creation need special consideration.

```java
@Component
public class MyRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources()
                .registerPattern("/data/*.json");
    }
}
```

You can run the native image from the target directory:

```shell
# Run the native image 
./target/scm-workshop

# Run the native image with a profile
./target/scm-workshop -Dspring.profiles.active=prod 
```

[Native Iamge Documentaiton](https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html)

## Testing

### Overusing @SpringBootTest

@SpringBootTest loads the entire application context for integration testing. While useful for verifying the full stack, overusing it can lead to bloated test suites, longer build times, and reduced efficiency.

When to Use `@SpringBootTest`:

* Integration Tests: To verify multiple components work together.
* End-to-End Scenarios: To validate the full request-response cycle.

When Not to Use @SpringBootTest

* Unit Tests: For testing a single class, @SpringBootTest is overkill. Use mocks to isolate dependencies.
* Layer-Specific Tests: Use slice annotations to test specific layers:
* Performance Considerations: @SpringBootTest can slow down tests. Slice tests are faster and more focused.

#### Available Slice Test Annotations

**Controller Layer: **
* `@WebMvcTest`: For testing MVC controllers without starting the full web server
    - Loads only web-related components (controllers, filters, etc.)
    - Useful for testing endpoint behavior and request/response handling

**Data Layer:**
* `@DataJpaTest`: For testing JPA repositories
    - Configures in-memory database
    - Loads JPA-related components only

* `@DataMongoTest`: For testing MongoDB repositories
    - Configures in-memory MongoDB
    - Perfect for testing MongoDB-specific operations and queries

* `@JdbcTest`: For testing JDBC operations
    - Sets up a basic JDBC environment
    - Useful when working directly with JdbcTemplate

**JSON Processing:**
* `@JsonTest`: For testing JSON serialization/deserialization
    - Loads JSON mapping infrastructure
    - Ideal for testing DTOs and JSON conversions

**Reactive Applications:**
* `@WebFluxTest`: For testing WebFlux controllers
    - Loads reactive web components
    - Perfect for testing reactive endpoints and flows

**External Services:** 
* `@RestClientTest`: For testing REST clients
    - Configures a mock web server
    - Useful for testing external API integrations without making real HTTP calls

**Custom Slices:**

You can create custom slice tests by combining annotations to test specific layers or components:
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = "com.example.service")
public @interface ServiceTest {}
```

**Best Practices**

1. Choose the most specific slice test for your needs
2. Mock dependencies in slice tests
3. Keep tests focused on a single layer or responsibility
4. Consider test execution time when choosing between full context and slice tests

By using slice tests strategically, you can create faster, more maintainable test suites without the performance hit.

### Insufficient Unit Testing

Unit tests are critical because they verify that individual components work correctly in isolation and catch bugs early in development, but they're often neglected because developers feel pressured to deliver features quickly and may view thorough testing as slowing down development velocity. Additionally, writing good unit tests requires a deep understanding of testing principles and extra time investment upfront, which can be challenging when facing tight deadlines.

I'm using Generative AI heavily for this right now

```java
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PostRepository postRepository() {
            return mock(PostRepository.class);
        }
    }

    @Test
    void findAll_ShouldReturnPosts() throws Exception {
        Post post1 = new Post(1L, "Content 1", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(1L, "Author 1", "author1@example.com", "hashedPassword1", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);
        Post post2 = new Post(2L, "Content 2", LocalDateTime.now(), List.of(), List.of(), List.of(), new User(2L, "Author 2", "author2@example.com", "hashedPassword2", null, List.of(), List.of(), Role.USER), List.of(), false, Visibility.PUBLIC);

        given(repository.findAll()).willReturn(Arrays.asList(post1, post2));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].content").value("Content 1"))
                .andExpect(jsonPath("$[1].content").value("Content 2"));
    }

    // More tests...

}
```

## Security

### Not Adopting Secure by Default ("Fail Closed")

When it comes to security, "default" settings are often your first line of defense. In the context of Spring Security, developers need to adopt a "secure by default" approach, also known as "fail closed." This means that, unless explicitly specified, all endpoints should be secured. The alternative—failing open—can lead to unintended access vulnerabilities, where sensitive parts of your application become accessible to unauthorized users.

The guiding principle should be: if access rules are not explicitly defined, deny access. This ensures that any oversight or mistake doesn’t inadvertently expose sensitive endpoints.

**Common Mistake: Failing Open**

A common mistake developers make is using `.anyRequest().permitAll()`. This configuration is effectively turning off security for all endpoints, making your entire application publicly accessible. It might seem convenient during testing or initial development, but it’s incredibly risky to leave such settings in place for production.

```java
// Example of what not to do
http
    .authorizeRequests()
    .anyRequest().permitAll();
```

In this example, no authentication is required for any request, leaving all resources open for public access. It’s easy to forget to modify this configuration, leading to severe security gaps.

**Best Practice: Failing Closed**

Instead, developers should adopt a more restrictive approach, either requiring authentication for all requests or denying access by default.

```java
// Example of secure configuration
http
    .authorizeRequests()
    .anyRequest().authenticated();
```

In this setup, every request requires authentication unless specified otherwise. This way, if a developer forgets to add a specific rule, the system defaults to secure behavior. Alternatively, you could use `.denyAll()` to completely block requests that do not have explicit permissions:

```java
// Example to deny all by default
http
    .authorizeRequests()
    .anyRequest().denyAll();
```

By adopting a fail-closed approach, you ensure that no resource is unintentionally exposed, and your application remains secure by default. It’s a small adjustment that can prevent significant vulnerabilities.

### Implementing your own JwtFilter

Another common mistake developers make is creating their own `JwtFilter` for handling JWT (JSON Web Token) authentication. While it might seem like a good idea to have full control over the JWT validation process, this approach can lead to numerous security pitfalls, such as incorrect token validation, missed security updates, or improper error handling.

Spring Security provides built-in support for handling JWTs through its OAuth2 Resource Server support. This allows developers to leverage well-tested, community-driven solutions that handle most of the complexities of JWT validation for you.

```java
// Example of what not to do
public class CustomJwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Custom JWT validation logic
    }
}
```

Creating a custom JWT filter like this can be error-prone and makes the application more difficult to maintain.

**Best Practice: Using OAuth2 Resource Server Support**

Instead of creating a custom JWT filter, developers should use the built-in OAuth2 Resource Server support in Spring Security. This feature simplifies the process of validating JWTs and ensures that security best practices are followed.

```java
// Example of secure configuration
http
    .oauth2ResourceServer()
    .jwt();
```

With this configuration, Spring Security takes care of validating the JWT, managing expiration, and handling errors. This approach not only reduces the risk of introducing vulnerabilities but also makes your application easier to maintain and keep up to date with security best practices.

By using OAuth2 Resource Server support, you can trust that your application is following industry standards for JWT validation, without the need for custom, error-prone code.

[Spring Security JWT Blog Post](https://www.danvega.dev/blog/spring-security-jwt)

## Resources

[This Repository](https://github.com/danvega/scm-workshop)

### Spring
- [Spring](https://spring.io)
- [Spring Academy](https://spring.academy/)
- [Spring Blog](https://spring.io/blog)
- [GitHub Spring Projects](https://github.com/spring-projects)
- [Spring Initializr](https://start.spring.io/)
- [Spring Calendar](https://calendar.spring.io/)

### Documentation

- [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring Framework API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/index.html)
- [Spring Boot API](https://docs.spring.io/spring-boot/docs/current/api/)
- [Spring Boot Guides](https://spring.io/guides)

### Dan Vega

- [Dan Vega](https://www.danvega.dev/)
- [YouTube](https://www.youtube.com/c/DanVega)
- [Twitter](https://twitter.com/therealdanvega)
- [BlueSky](https://bsky.app/profile/danvega.dev)
- [LinkedIn](https://www.linkedin.com/in/danvega/)
- [GitHub](https://github.com/danvega/)

## Notes

- Docker Desktop needs to be running
- Browswer Tabs
  - [This Repository](https://github.com/danvega/scm-workshop)
  - [Spring Initializr](https://start.spring.io/)
    - Spring Init Features 🤩
- Branches
  - main
  - start-here
  - data-access-start
  - web-apis-start
  - performance-start