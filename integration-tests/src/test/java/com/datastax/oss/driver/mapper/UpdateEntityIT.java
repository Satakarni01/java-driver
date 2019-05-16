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
package com.datastax.oss.driver.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.testinfra.CassandraRequirement;
import com.datastax.oss.driver.api.testinfra.ccm.CcmRule;
import com.datastax.oss.driver.api.testinfra.session.SessionRule;
import com.datastax.oss.driver.categories.ParallelizableTests;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@Category(ParallelizableTests.class)
@CassandraRequirement(min = "3.4", description = "Creates a SASI index")
public class UpdateEntityIT extends InventoryITBase {

  private static CcmRule ccm = CcmRule.getInstance();

  private static SessionRule<CqlSession> sessionRule = SessionRule.builder(ccm).build();

  @ClassRule public static TestRule chain = RuleChain.outerRule(ccm).around(sessionRule);

  private static ProductDao dao;

  @BeforeClass
  public static void setup() {
    CqlSession session = sessionRule.session();

    for (String query : createStatements()) {
      session.execute(
          SimpleStatement.builder(query).setExecutionProfile(sessionRule.slowProfile()).build());
    }

    InventoryMapper inventoryMapper = new UpdateEntityIT_InventoryMapperBuilder(session).build();
    dao = inventoryMapper.productDao(sessionRule.keyspace());
  }

  @Before
  public void clearProductData() {
    CqlSession session = sessionRule.session();
    session.execute(
        SimpleStatement.builder("TRUNCATE product")
            .setExecutionProfile(sessionRule.slowProfile())
            .build());
  }

  @Test
  public void should_insert_entity() {
    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();

    dao.update(FLAMETHROWER);
    assertThat(dao.findById(FLAMETHROWER.getId())).isEqualTo(FLAMETHROWER);
  }

  @Test
  public void should_insert_entity_asynchronously() {
    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();

    CompletableFutures.getUninterruptibly(dao.updateAsync(FLAMETHROWER));
    assertThat(dao.findById(FLAMETHROWER.getId())).isEqualTo(FLAMETHROWER);
  }

  //  @Ignore("Using not work yet")
  //  @Test
  //  public void should_insert_entity_with_custom_clause() {
  //    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();
  //
  //    long timestamp = 1234;
  //    dao.updateWithBoundTimestamp(FLAMETHROWER, timestamp);
  //
  //    CqlSession session = sessionRule.session();
  //    Row row =
  //        session
  //            .execute(
  //                SimpleStatement.newInstance(
  //                    "SELECT WRITETIME(description) FROM product WHERE id = ?",
  //                    FLAMETHROWER.getId()))
  //            .one();
  //    assert row != null;
  //    long writeTime = row.getLong(0);
  //    assertThat(writeTime).isEqualTo(timestamp);
  //  }

  //  @Ignore("using not working yet")
  //  @Test
  //  public void should_insert_entity_with_custom_clause_asynchronously() {
  //    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();
  //
  //    long timestamp = 1234;
  //    CompletableFutures.getUninterruptibly(
  //        dao.updateAsyncWithBoundTimestamp(FLAMETHROWER, timestamp));
  //
  //    CqlSession session = sessionRule.session();
  //    Row row =
  //        session
  //            .execute(
  //                SimpleStatement.newInstance(
  //                    "SELECT WRITETIME(description) FROM product WHERE id = ?",
  //                    FLAMETHROWER.getId()))
  //            .one();
  //    assert row != null;
  //    long writeTime = row.getLong(0);
  //    assertThat(writeTime).isEqualTo(timestamp);
  //  }
  //
  @Test
  public void should_insert_entity_if_exists() {
    dao.update(FLAMETHROWER);
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(dao.updateIfExists(otherProduct).wasApplied()).isEqualTo(true);
  }

  @Test
  public void should_not_insert_entity_if_not_exists() {
    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(dao.updateIfExists(otherProduct).wasApplied()).isEqualTo(false);
  }

  @Test
  public void should_insert_entity_if_exists_asynchronously() {
    dao.update(FLAMETHROWER);
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(
            CompletableFutures.getUninterruptibly(dao.updateAsyncIfExists(otherProduct))
                .wasApplied())
        .isEqualTo(true);
  }

  @Test
  public void should_not_insert_entity_if_not_exists_asynchronously() {
    assertThat(dao.findById(FLAMETHROWER.getId())).isNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(
            CompletableFutures.getUninterruptibly(dao.updateAsyncIfExists(otherProduct))
                .wasApplied())
        .isEqualTo(false);
  }

  @Test
  public void should_insert_entity_if_condition_is_met() {
    dao.update(
        new Product(FLAMETHROWER.getId(), "Description for length 10", new Dimensions(10, 1, 1)));
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(dao.updateIfLength(otherProduct, 10).wasApplied()).isEqualTo(true);
  }

  @Test
  public void should_not_insert_entity_if_condition_is_not_met() {
    dao.update(
        new Product(FLAMETHROWER.getId(), "Description for length 10", new Dimensions(10, 1, 1)));
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(dao.updateIfLength(otherProduct, 20).wasApplied()).isEqualTo(false);
  }

  @Test
  public void should_async_insert_entity_if_condition_is_met() {
    dao.update(
        new Product(FLAMETHROWER.getId(), "Description for length 10", new Dimensions(10, 1, 1)));
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(
            CompletableFutures.getUninterruptibly(dao.updateIfLengthAsync(otherProduct, 10))
                .wasApplied())
        .isEqualTo(true);
  }

  @Test
  public void should_not_async_insert_entity_if_condition_is_not_met() {
    dao.update(
        new Product(FLAMETHROWER.getId(), "Description for length 10", new Dimensions(10, 1, 1)));
    assertThat(dao.findById(FLAMETHROWER.getId())).isNotNull();

    Product otherProduct =
        new Product(FLAMETHROWER.getId(), "Other description", new Dimensions(1, 1, 1));
    assertThat(
            CompletableFutures.getUninterruptibly(dao.updateIfLengthAsync(otherProduct, 20))
                .wasApplied())
        .isEqualTo(false);
  }

  @Mapper
  public interface InventoryMapper {
    @DaoFactory
    ProductDao productDao(@DaoKeyspace CqlIdentifier keyspace);
  }

  @Dao
  public interface ProductDao {

    @Update(whereClause = "id = :id")
    void update(Product product);

    //
    //    @Update(whereClause = "id = :id", customUsingClause = "USING TIMESTAMP :timestamp")
    //    void updateWithBoundTimestamp(Product product, long timestamp);

    @Update(whereClause = "id = :id", ifExists = true)
    ResultSet updateIfExists(Product product);

    @Update(whereClause = "id = :id")
    CompletableFuture<Void> updateAsync(Product product);

    @Update(whereClause = "id = :id", ifCondition = "dimensions.length = :length")
    ResultSet updateIfLength(Product product, int length);

    @Update(whereClause = "id = :id", ifCondition = "dimensions.length = :length")
    CompletableFuture<AsyncResultSet> updateIfLengthAsync(Product product, int length);

    //    @Update(whereClause = "id = :id", customUsingClause = "USING TIMESTAMP :timestamp")
    //    CompletableFuture<Void> updateAsyncWithBoundTimestamp(Product product, long timestamp);
    //
    @Update(whereClause = "id = :id", ifExists = true)
    CompletableFuture<AsyncResultSet> updateAsyncIfExists(Product product);

    @Select
    Product findById(UUID productId);
  }
}