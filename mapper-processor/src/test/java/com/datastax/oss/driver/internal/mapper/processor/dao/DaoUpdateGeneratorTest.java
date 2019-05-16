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

import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DaoUpdateGeneratorTest extends DaoMethodGeneratorTest {

  private static final AnnotationSpec UPDATE_ANNOTATION_WITH_WHERE_CLAUSE =
      AnnotationSpec.builder(Update.class).addMember("whereClause", "$S", "id = :id").build();

  @Test
  @Override
  @UseDataProvider("invalidSignatures")
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  @DataProvider
  public static Object[][] invalidSignatures() {
    return new Object[][] {
      {
        "Wrong number of parameters: Update methods must have at least one",
        MethodSpec.methodBuilder("update")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(UPDATE_ANNOTATION_WITH_WHERE_CLAUSE)
            .build(),
      },
      {
        "Invalid parameter type: Update methods must take the entity to update as the first parameter",
        MethodSpec.methodBuilder("update")
            .addAnnotation(UPDATE_ANNOTATION_WITH_WHERE_CLAUSE)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(String.class, "a").build())
            .build(),
      },
      {
        "Invalid return type: Update methods must return either void or the entity class (possibly wrapped in a CompletionStage/CompletableFuture)",
        MethodSpec.methodBuilder("update")
            .addAnnotation(UPDATE_ANNOTATION_WITH_WHERE_CLAUSE)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(TypeName.INT)
            .build(),
      },
      {
        "You cannot specify both ifExists(true) and ifCondition(1 = 1) for Update method.",
        MethodSpec.methodBuilder("update")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(Update.class)
                    .addMember("whereClause", "$S", "id = :id")
                    .addMember("ifExists", "true")
                    .addMember("ifCondition", "$S", "1 = 1")
                    .build())
            .addParameter(ParameterSpec.builder(ENTITY_CLASS_NAME, "entity").build())
            .returns(TypeName.VOID)
            .build(),
      },
    };
  }
}
