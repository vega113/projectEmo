# Anorm Database Operations Guide

## Introduction
This guide provides a comprehensive overview of how database operations are handled in a Scala project using Anorm. It covers the architecture, components, and best practices for implementing database access in a Play Framework application.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Database Configuration](#database-configuration)
3. [Model Definitions](#model-definitions)
4. [Data Access Objects (DAOs)](#data-access-objects-daos)
5. [Service Layer](#service-layer)
6. [Connection Management](#connection-management)
7. [SQL Queries with Anorm](#sql-queries-with-anorm)
8. [Database Migrations](#database-migrations)
9. [Best Practices](#best-practices)

## Architecture Overview
The database operations in this project follow a layered architecture:

1. **Model Layer**: Defines case classes that represent database entities
2. **DAO Layer**: Handles direct database access using Anorm
3. **Service Layer**: Provides business logic and transaction management
4. **Controller Layer**: Handles HTTP requests and delegates to services

This separation of concerns ensures maintainability and testability of the codebase.

## Database Configuration
Database configuration is defined in `application.conf`:

```hocon
db.default {
  driver = "com.mysql.cj.jdbc.Driver"
  url = "jdbc:mysql://localhost/emodb?useSSL=false&serverTimezone=UTC"
  url = ${?DATABASE_URL}
  username = "wavy"
  username = ${?DATABASE_USERNAME}
  password = "password"
  password = ${?DATABASE_PASSWORD}
  hikaricp.maximumPoolSize = ${?DATABASE_POOL_SIZE}
  hikaricp.maximumPoolSize = 3
}
```

Key points:
- Uses MySQL as the database
- Configures HikariCP for connection pooling
- Allows environment variable overrides for deployment flexibility
- Defines a dedicated dispatcher for database operations

## Model Definitions
Models are defined as case classes in Scala, with companion objects that provide Anorm parsers:

```scala
case class AiThread(
  id: Option[Long],
  externalId: String,
  userId: Long,
  threadType: String,
  isDeleted: Boolean,
  created: Option[LocalDateTime],
)

object AiThread {
  implicit val aiThreadFormat: Format[AiThread] = Json.format[AiThread]
  implicit val parser: RowParser[AiThread] = Macro.namedParser[AiThread](ColumnNaming.SnakeCase)
}
```

Key points:
- Case classes represent database entities
- Companion objects provide JSON formats for serialization/deserialization
- Anorm parsers are defined using `Macro.namedParser` with `ColumnNaming.SnakeCase`
- This automatically maps snake_case database columns to camelCase Scala fields

## Data Access Objects (DAOs)
DAOs handle direct database access using Anorm:

```scala
class AiDao {
  def fetchThreadById(id: Long)(implicit connection: Connection): Option[AiThread] = {
    SQL(
      """
        |SELECT id, external_id, user_id, thread_type, is_deleted, created
        |FROM ai_threads
        |WHERE id = {id}
        |""".stripMargin
    ).on(
      "id" -> id
    ).as(AiThread.parser.singleOpt)
  }
  
  def insertAiThread(aiThread: AiThread)(implicit connection: Connection): AiThread = {
    val id: Option[Long] = SQL(
      """
        |INSERT INTO ai_threads (external_id, user_id, thread_type)
        |VALUES ({externalId}, {userId}, {threadType})
        |""".stripMargin
    ).on(
      "externalId" -> aiThread.externalId,
      "userId" -> aiThread.userId,
      "threadType" -> aiThread.threadType
    ).executeInsert()
    aiThread.copy(id = id)
  }
}
```

Key points:
- Each DAO method takes an implicit `Connection` parameter
- SQL queries are defined as multi-line strings with `.stripMargin` for readability
- Parameters are specified using curly braces in the SQL string (e.g., `{id}`)
- Parameters are bound using the `.on()` method
- Results are parsed using the parsers defined in the model companion objects
- For select operations, `.as()` is used with a parser to convert results to Scala objects
- For insert operations, `.executeInsert()` returns the generated ID

## Service Layer
Services provide business logic and transaction management:

```scala
@ImplementedBy(classOf[AiDbServiceImpl])
trait AiDbService {
  def saveAiThread(aiThread: AiThread): Future[AiThread]
  def fetchThreadById(id: Long): Future[Option[AiThread]]
}

class AiDbServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext, aiDao: AiDao) extends AiDbService {
  override def saveAiThread(aiThread: AiThread): Future[AiThread] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.insertAiThread(aiThread))
    })
  }
  
  override def fetchThreadById(id: Long): Future[Option[AiThread]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchThreadById(id))
    })
  }
}
```

Key points:
- Services are defined as traits with implementation classes
- Dependency injection is used to provide dependencies
- The `@ImplementedBy` annotation binds the trait to its implementation
- Services use `databaseExecutionContext.withConnection` to manage database connections
- All operations return `Future` objects for asynchronous execution
- Services delegate actual database operations to DAOs
- Error handling is implemented at the service layer

## Connection Management
Connection management is handled by a custom execution context:

```scala
@ImplementedBy(classOf[DatabaseExecutionContextImpl])
trait DatabaseExecutionContext {
  def withConnection[A](block: Connection => A): A
}

class DatabaseExecutionContextImpl @Inject()(db: Database, actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher") with DatabaseExecutionContext {

  def withConnection[A](block: Connection => A): A = {
    db.withConnection { connection =>
      block(connection)
    }
  }
}
```

Key points:
- A custom execution context is defined for database operations
- It extends Play's `CustomExecutionContext` to use a dedicated thread pool
- The `withConnection` method manages database connections
- It ensures connections are properly acquired and released
- The execution context is configured in `application.conf` under `database.dispatcher`

## SQL Queries with Anorm
Anorm provides a simple way to write SQL queries in Scala:

### Select Queries
```scala
SQL(
  """
    |SELECT id, external_id, user_id, thread_type, is_deleted, created
    |FROM ai_threads
    |WHERE id = {id}
    |""".stripMargin
).on(
  "id" -> id
).as(AiThread.parser.singleOpt)
```

### Insert Queries
```scala
SQL(
  """
    |INSERT INTO ai_threads (external_id, user_id, thread_type)
    |VALUES ({externalId}, {userId}, {threadType})
    |""".stripMargin
).on(
  "externalId" -> aiThread.externalId,
  "userId" -> aiThread.userId,
  "threadType" -> aiThread.threadType
).executeInsert()
```

### Update Queries
```scala
SQL(
  """
    |UPDATE ai_assistants
    |SET is_deleted = true
    |WHERE external_id = {externalId}
    |""".stripMargin
).on(
  "externalId" -> externalId
).executeInsert()
```

Key points:
- SQL queries are written as strings with parameters in curly braces
- Parameters are bound using the `.on()` method
- For select queries, `.as()` is used with a parser to convert results
- For insert queries, `.executeInsert()` returns the generated ID
- For update queries, `.executeUpdate()` returns the number of affected rows

## Database Migrations
Database migrations are managed using Liquibase:

```xml
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd">

    <include file="conf/db/changelog-create-tables.xml" />
    <include file="conf/db/changelog-import-data.xml" />
    <include file="conf/db/milestones/milestone_1.xml" />
</databaseChangeLog>
```

Key points:
- Liquibase is used for database schema migrations
- Migrations are defined in XML files
- A master changelog includes all other changelogs
- Each changeset has an ID and author for tracking
- Changesets can create tables, add columns, modify data, etc.
- Liquibase ensures migrations are applied in order and only once

## Best Practices

### 1. Separation of Concerns
- Keep models, DAOs, and services in separate files
- Models should represent database entities
- DAOs should handle database access
- Services should provide business logic

### 2. Connection Management
- Always use `withConnection` to ensure connections are properly released
- Use a dedicated thread pool for database operations
- Configure connection pooling appropriately for your application

### 3. Error Handling
- Handle database errors at the service layer
- Use Scala's `Try` and pattern matching for error handling
- Log errors with appropriate context

### 4. Asynchronous Operations
- Return `Future` objects from service methods
- Use a dedicated execution context for database operations
- Avoid blocking the main application thread

### 5. SQL Queries
- Use multi-line strings with `.stripMargin` for readability
- Use named parameters instead of positional parameters
- Include enough context in the query for maintainability

### 6. Row Parsing
- Use `Macro.namedParser` with `ColumnNaming.SnakeCase` for automatic mapping
- Define parsers in model companion objects
- Use `.singleOpt` for queries that might return no results
- Use `.single` for queries that must return exactly one result

### 7. Database Migrations
- Use Liquibase for database schema migrations
- Give each changeset a unique ID and author
- Include comments explaining complex changes
- Test migrations before applying them to production

By following these patterns and best practices, you can build a robust and maintainable database layer for your Scala application using Anorm.