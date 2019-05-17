/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.mapper.annotations;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Annotates a {@link Dao} method that updates an instance of an {@link Entity}-annotated class.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;Dao
 * public interface ProductDao {
 *   &#64;Update
 *   void update(Product product);
 * }
 * </pre>
 *
 * <h3>Parameters</h3>
 *
 * The first parameter must be the entity to update.
 *
 * <p>If the query has a {@linkplain #customUsingClause()} custom clause} with placeholders, the
 * method must have corresponding additional parameters (same name, and a compatible Java type):
 *
 * <pre>
 * &#64;Update(customUsingClause = "USING TTL :ttl")
 * void updateWithTtl(Product product, int ttl);
 * </pre>
 *
 * <h3>Return type</h3>
 *
 * The method can return:
 *
 * <ul>
 *   <li>{@code void}.
 *   <li>a {@link ResultSet} - The method will return the raw query result, without any conversion.
 *       Useful for queries that use {@code customIfClause} or {@code ifExists}. The status of the
 *       update operation can be retrieved via {@code wasApplied()} method
 *       <pre>
 * &#64;Update(ifExists = true)
 * ResultSet updateIfExists(Product product);
 *       </pre>
 *   <li>a {@link Boolean} - when you are not interested in retrieving the whole ResultSet but only
 *       wasApply result. The {@link ResultSet} will be automatically mapped to Boolean.
 *       <pre>
 *     &#64;Update(ifExists = true)
 *     boolean updateReturnWasApplied(Product product);
 *   </pre>
 *   <li>a {@link CompletionStage} or {@link CompletableFuture} of any of the above. The mapper will
 *       execute the query asynchronously. Note that for result set, you need to switch to the
 *       asynchronous equivalent {@link AsyncResultSet}
 *       <pre>
 * &#64;Update
 * CompletionStage&lt;Void&gt; update(Product product);
 *
 * &#64;Update(ifExists = true)
 * CompletableFuture&lt;AsyncResultSet&gt; updateIfExists(Product product);
 *
 * &#64;Update(ifExists = true)
 * CompletableFuture&lt;Boolean&gt; updateIfExistsReturnWasApplied(Product product);
 *       </pre>
 * </ul>
 *
 * <h3>Target keyspace and table</h3>
 *
 * If a keyspace was specified when creating the DAO (see {@link DaoFactory}), then the generated
 * query targets that keyspace. Otherwise, it doesn't specify a keyspace, and will only work if the
 * mapper was built from a {@link Session} that has a {@linkplain
 * SessionBuilder#withKeyspace(CqlIdentifier) default keyspace} set.
 *
 * <p>If a table was specified when creating the DAO, then the generated query targets that table.
 * Otherwise, it uses the default table name for the entity (which is determined by the name of the
 * entity class and the naming convention).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Update {

  /**
   * Whether to append an IF EXISTS clause at the end of the generated UPDATE query.
   *
   * <p>This is mutually exclusive with {@link #customIfClause()} (if both are set, the mapper
   * processor will generate a compile-time error).
   */
  boolean ifExists() default false;

  /**
   * A custom IF clause for the UPDATE query.
   *
   * <p>This is mutually exclusive with {@link #ifExists()} (if both are set, the mapper processor
   * will generate a compile-time error).
   *
   * <p>If this is not empty, it gets appended at the end of the generated query. Note that the
   * provided string must start with the {@code IF} keyword.
   *
   * <p>This clause can contain placeholders that will be bound with the method's parameters; see
   * the top-level javadocs of this class for more explanations.
   */
  String customIfClause() default "";

  /**
   * A custom USING clause for the UPDATE query.
   *
   * <p>The default mapper code generates a query of the form {@code UPDATE table (...) USING (TTL |
   * TIMESTAMP)}. If this element is a non empty string, it gets appended. Therefore it can be used
   * to add a {@code USING TTL} or {@code USING TIMESTAMP} clause.
   *
   * <p>This clause can contain placeholders that will be bound with the method's parameters; see
   * the top-level javadocs of this class for more explanations.
   */
  String customUsingClause() default "";
}
