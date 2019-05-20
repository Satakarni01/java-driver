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
  @UseDataProvider("usingTimestampProvider")
  public void should_process_timestamp(String timestamp, String expected) {
    // given
    DaoUpdateMethodGenerator daoUpdateMethodGenerator =
        new DaoUpdateMethodGenerator(null, null, null);
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    // when
    daoUpdateMethodGenerator.maybeAddTimestamp(timestamp, builder);

    // then
    assertThat(builder.build().code).isEqualTo(CodeBlock.of(expected));
  }

  @Test
  @UseDataProvider("usingTtlProvider")
  public void should_process_ttl(String ttl, String expected) {
    // given
    DaoUpdateMethodGenerator daoUpdateMethodGenerator =
        new DaoUpdateMethodGenerator(null, null, null);
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();

    // when
    daoUpdateMethodGenerator.maybeAddTtl(ttl, builder);

    // then
    assertThat(builder.build().code).isEqualTo(CodeBlock.of(expected));
  }

  @DataProvider
  public static Object[][] usingTimestampProvider() {
    return new Object[][] {
      {"1", ".usingTimestamp(1)"},
      {
        ":ts",
        ".usingTimestamp(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ts\"))"
      },
      {"1", ".usingTimestamp(1)"},
      {
        ":TS",
        ".usingTimestamp(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"TS\"))"
      },
    };
  }

  @DataProvider
  public static Object[][] usingTtlProvider() {
    return new Object[][] {
      {"1", ".usingTtl(1)"},
      {
        ":ttl",
        ".usingTtl(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"ttl\"))"
      },
      {"1", ".usingTtl(1)"},
      {
        ":TTL",
        ".usingTtl(com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker(\"TTL\"))"
      },
    };
  }
}
