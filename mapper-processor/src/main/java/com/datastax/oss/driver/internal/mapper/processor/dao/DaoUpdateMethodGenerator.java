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

import static com.datastax.oss.driver.internal.mapper.processor.dao.ReturnTypeKind.FUTURE_OF_ASYNC_RESULT_SET;
import static com.datastax.oss.driver.internal.mapper.processor.dao.ReturnTypeKind.FUTURE_OF_VOID;
import static com.datastax.oss.driver.internal.mapper.processor.dao.ReturnTypeKind.RESULT_SET;
import static com.datastax.oss.driver.internal.mapper.processor.dao.ReturnTypeKind.VOID;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.util.generation.GeneratedCodePatterns;
import com.datastax.oss.driver.internal.querybuilder.update.DefaultUpdate;
import com.datastax.oss.driver.shaded.guava.common.annotations.VisibleForTesting;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class DaoUpdateMethodGenerator extends DaoMethodGenerator {

  private static final EnumSet<ReturnTypeKind> SUPPORTED_RETURN_TYPES =
      EnumSet.of(VOID, FUTURE_OF_VOID, RESULT_SET, FUTURE_OF_ASYNC_RESULT_SET);
  private static final Pattern USING_TIMESTAMP_VALUE_PATTER =
      Pattern.compile("(?i)USING TIMESTAMP (\\d+$)");
  private static final Pattern USING_TIMESTAMP_BIND_MARKER_PATTERN =
      Pattern.compile("(?i)USING TIMESTAMP :(\\w+$)");
  private static final Pattern USING_TTL_VALUE_PATTER = Pattern.compile("(?i)USING TTL (\\d+)$");
  private static final Pattern USING_TTL_BIND_MARKER_PATTERN =
      Pattern.compile("(?i)USING TTL :(\\w+)$");

  public DaoUpdateMethodGenerator(
      ExecutableElement methodElement,
      DaoImplementationSharedCode enclosingClass,
      ProcessorContext context) {
    super(methodElement, enclosingClass, context);
  }

  @Override
  public Optional<MethodSpec> generate() {

    // Validate the parameters:
    // - the first one must be the entity.
    // - the others are completely free-form (they'll be used as additional bind variables)
    if (methodElement.getParameters().isEmpty()) {
      context
          .getMessager()
          .error(
              methodElement,
              "Wrong number of parameters: %s methods must have at least one",
              Update.class.getSimpleName());
      return Optional.empty();
    }
    VariableElement firstParameter = methodElement.getParameters().get(0);
    TypeElement entityElement = asEntityElement(firstParameter);
    if (entityElement == null) {
      context
          .getMessager()
          .error(
              methodElement,
              "Invalid parameter type: "
                  + "%s methods must take the entity to update as the first parameter",
              Update.class.getSimpleName());
      return Optional.empty();
    }

    // Validate the return type:
    ReturnType returnType = parseReturnType(methodElement.getReturnType());
    if (!SUPPORTED_RETURN_TYPES.contains(returnType.kind)) {
      context
          .getMessager()
          .error(
              methodElement,
              "Invalid return type: %s methods must return either void or the entity class "
                  + "(possibly wrapped in a CompletionStage/CompletableFuture)",
              Update.class.getSimpleName());
      return Optional.empty();
    }

    // Generate the method:
    String helperFieldName = enclosingClass.addEntityHelperField(ClassName.get(entityElement));
    String statementName =
        enclosingClass.addPreparedStatement(
            methodElement,
            (methodBuilder, requestName) ->
                generatePrepareRequest(methodBuilder, requestName, helperFieldName));

    MethodSpec.Builder methodBuilder = GeneratedCodePatterns.override(methodElement);

    if (returnType.kind.isAsync) {
      methodBuilder.beginControlFlow("try");
    }
    methodBuilder.addStatement(
        "$T boundStatementBuilder = $L.boundStatementBuilder()",
        BoundStatementBuilder.class,
        statementName);

    List<? extends VariableElement> parameters = methodElement.getParameters();
    String entityParameterName = parameters.get(0).getSimpleName().toString();
    methodBuilder.addStatement(
        "$L.set($L, boundStatementBuilder)", helperFieldName, entityParameterName);

    // Handle all remaining parameters as additional bound values
    if (parameters.size() > 1) {
      GeneratedCodePatterns.bindParameters(
          parameters.subList(1, parameters.size()), methodBuilder, enclosingClass, context);
    }

    methodBuilder
        .addCode("\n")
        .addStatement("$T boundStatement = boundStatementBuilder.build()", BoundStatement.class);

    returnType.kind.addExecuteStatement(methodBuilder, helperFieldName);

    if (returnType.kind.isAsync) {
      methodBuilder
          .nextControlFlow("catch ($T t)", Throwable.class)
          .addStatement("return $T.failedFuture(t)", CompletableFutures.class)
          .endControlFlow();
    }
    return Optional.of(methodBuilder.build());
  }

  private void generatePrepareRequest(
      MethodSpec.Builder methodBuilder, String requestName, String helperFieldName) {
    methodBuilder.addCode(
        "$[$1T $2L = $1T.newInstance((($4T)$3L.update()",
        SimpleStatement.class,
        requestName,
        helperFieldName,
        DefaultUpdate.class);
    Update annotation = methodElement.getAnnotation(Update.class);

    maybeAddUsingClause(methodBuilder, annotation.customUsingClause());

    maybeAddIfClause(methodBuilder, annotation);

    methodBuilder.addCode(")$];\n");
  }

  @VisibleForTesting
  void maybeAddUsingClause(MethodSpec.Builder methodBuilder, String customUsingClause) {
    if (customUsingClause.isEmpty()) {
      methodBuilder.addCode(")");
      return;
    }
    Matcher usingTimestampBindMakerMatcher =
        USING_TIMESTAMP_BIND_MARKER_PATTERN.matcher(customUsingClause);
    if (usingTimestampBindMakerMatcher.matches()) {
      methodBuilder.addCode(
          ".usingTimestamp($1T.bindMarker($2S)))",
          QueryBuilder.class,
          usingTimestampBindMakerMatcher.group(1));
      return;
    }
    Matcher usingTimestampValueMatcher = USING_TIMESTAMP_VALUE_PATTER.matcher(customUsingClause);
    if (usingTimestampValueMatcher.matches()) {
      methodBuilder.addCode(".usingTimestamp($L))", usingTimestampValueMatcher.group(1));
      return;
    }
    Matcher usingTtlBindMakerMatcher = USING_TTL_BIND_MARKER_PATTERN.matcher(customUsingClause);
    if (usingTtlBindMakerMatcher.matches()) {
      methodBuilder.addCode(
          ".usingTtl($1T.bindMarker($2S)))", QueryBuilder.class, usingTtlBindMakerMatcher.group(1));
      return;
    }
    Matcher usingTtlValueMatcher = USING_TTL_VALUE_PATTER.matcher(customUsingClause);
    if (usingTtlValueMatcher.matches()) {
      methodBuilder.addCode(".usingTtl($L))", usingTtlValueMatcher.group(1));
      return;
    }

    // no match - it means that using clause was incorrect
    context
        .getMessager()
        .error(
            methodElement,
            "customUsingClause: %s on the: %s method is incorrect.",
            customUsingClause,
            Update.class.getSimpleName());
  }

  private void maybeAddIfClause(MethodSpec.Builder methodBuilder, Update annotation) {
    if (annotation.ifExists() && !annotation.customIfClause().isEmpty()) {
      context
          .getMessager()
          .error(
              methodElement,
              "Invalid annotation parameters: %s cannot have both ifExists and customIfClause",
              Update.class.getSimpleName());
    }

    if (annotation.ifExists()) {
      methodBuilder.addCode(".ifExists()").addCode(".asCql()");
    } else if (!annotation.customIfClause().isEmpty()) {
      methodBuilder.addCode(".asCql() + $S", " " + annotation.customIfClause());
    } else {
      methodBuilder.addCode(".asCql()");
    }
  }
}
