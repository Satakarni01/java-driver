package com.datastax.oss.driver.internal.mapper.processor.dao;

import static com.google.common.truth.Truth.assertThat;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DaoUpdateMethodGeneratorTest {

  @Test
  @UseDataProvider("usingClauseProvider")
  public void should_process_using_clause(String usingClause, String expected) {
    // given
    DaoUpdateMethodGenerator daoUpdateMethodGenerator =
        new DaoUpdateMethodGenerator(null, null, null);
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    // when
    daoUpdateMethodGenerator.maybeAddUsingClause(builder, usingClause);

    // then
    assertThat(builder.build().code).isEqualTo(CodeBlock.of(expected));
  }

  @DataProvider
  public static Object[][] usingClauseProvider() {
    return new Object[][] {
      {"USING TIMESTAMP 1", ".usingTimestamp(1))"},
      {
        "USING TIMESTAMP :ts",
        ".usingTimestamp(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ts\")))"
      },
      {"USING TTL 1", ".usingTtl(1))"},
      {
        "USING TTL :ttl",
        ".usingTtl(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ttl\")))"
      },
      {"using timestamp 1", ".usingTimestamp(1))"},
      {
        "using timestamp :ts",
        ".usingTimestamp(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ts\")))"
      },
      {"using ttl 1", ".usingTtl(1))"},
      {
        "using ttl :ttl",
        ".usingTtl(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ttl\")))"
      }
    };
  }
}
