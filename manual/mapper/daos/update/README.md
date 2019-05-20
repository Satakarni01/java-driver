## Update methods

Annotate a DAO method with [@Update] to generate a query that upate an [Entity](../../entities):

```java
@Dao
public interface ProductDao {
  @Update
  void update(Product product);
}
```

### Parameters

The first parameter must be the entity to update.

If the annotation doesn't have a `customWhereClause`, the mapper defaults to a where by primary
key (partition key + clustering columns). The method's parameters must match the types of the
[primary key columns](../../entities/#primary-key-columns), in the exact order (as defined by the
[@PartitionKey] and [@ClusteringColumn] annotations). The parameter names don't necessarily need to
match the names of the columns.

If the annotation has a `customWhereClause`, it completely replaces the WHERE clause. The provided
string can contain named placeholders. In that case, the method must have a corresponding parameter
for each, with the same name and a compatible Java type.

```java
@Update(customWhereClause = "description LIKE :description")
boolean updateByDescription(Product product, String description);
```

If the query has a custom timestamp or ttl with placeholders, the method must have corresponding additional
parameters (same name, and a compatible Java type):

```java
@Update(timestamp = ":timestamp")
void updateWithTimestamp(Product product, int timestamp);

@Update(ttl = ":ttl")
void updateWithTtl(Product product, int ttl);
```

An optional IF clause can be appended to the generated query. It can contain placeholders, for which
the method must have corresponding parameters (same name, and a compatible Java type):

```java
@Update(customIfClause = "description = :expectedDescription")
void updateIfDescriptionMatches(Product product, String expectedDescription);
```

An optional IF EXISTS clause at the end of the generated UPDATE query.

This is mutually exclusive with customIfClause (if both are set, the mapper processor will generate a compile-time error):


```java
@Update(ifExists = true)
void updateIfExists(Product product);
```



### Return type

The method can return:

* `void`.

* the [ResultSet] - returns the raw query result, without any conversion.
  Useful for queries that use customIfClause or ifExists.
  The status of the update operation can be retrieved via wasApplied() method
   ```java
   @Update(ifExists = true)
   ResultSet updateIfExists(Product product);
   ```

* a [Boolean] - when you are not interested in retrieving the whole ResultSet but only wasApply
  result. The [ResultSet] will be automatically mapped to Boolean.
  
  ```java
  @Update(ifExists = true)
  boolean updateReturnWasApplied(Product product);
  ```
    
* a [CompletionStage] or [CompletableFuture] of any of the above. The mapper will execute the query
  asynchronously. 
  Note that for result set, you need to switch to the asynchronous equivalent [AsyncResultSet]

    ```java
    @Update
    CompletionStage<Void> update(Product product);

    @Update(ifExists = true)
    CompletableFuture<AsyncResultSet> updateIfExists(Product product);

    @Update(ifExists = true)
    CompletableFuture<Boolean> updateIfExists(Product product);
    ```

### Target keyspace and table

If a keyspace was specified [when creating the DAO](../../mapper/#dao-factory-methods), then the
generated query targets that keyspace. Otherwise, it doesn't specify a keyspace, and will only work
if the mapper was built from a session that has a [default keyspace] set.

If a table was specified when creating the DAO, then the generated query targets that table.
Otherwise, it uses the default table name for the entity (which is determined by the name of the
entity class and the naming convention).

[default keyspace]: https://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/core/session/SessionBuilder.html#withKeyspace-com.datastax.oss.driver.api.core.CqlIdentifier-
[@Update]:          https://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/mapper/annotations/Update.html

[AsyncResultSet]: http://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/core/cql/AsyncResultSet.html
[Boolean]: https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/Boolean.html
[CompletionStage]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html
[CompletableFuture]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html
[ResultSet]:            http://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/core/cql/ResultSet.html
