package com.datastax.oss.driver.internal.mapper.processor.entity;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.querybuilder.update.DefaultUpdate;
import com.squareup.javapoet.MethodSpec;
import java.util.Optional;
import javax.lang.model.element.Modifier;

public class EntityHelperUpdateWhereByPrimaryKeyMethodGenerator implements MethodGenerator {

  private final EntityDefinition entityDefinition;

  EntityHelperUpdateWhereByPrimaryKeyMethodGenerator(
      EntityDefinition entityDefinition,
      EntityHelperGenerator enclosingClass,
      ProcessorContext context) {
    this.entityDefinition = entityDefinition;
  }

  @Override
  public Optional<MethodSpec> generate() {
    MethodSpec.Builder methodBuilder =
        MethodSpec.methodBuilder("updateWhereByPrimaryKey")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(DefaultUpdate.class)
            .addCode("$[return (($T)updateStart()", DefaultUpdate.class);

    for (PropertyDefinition property : entityDefinition.getPrimaryKey()) {
      methodBuilder.addCode(
          "\n.where($1T.column($2S).isEqualTo($3T.bindMarker($2S)))",
          Relation.class,
          property.getCqlName(),
          QueryBuilder.class);
    }

    methodBuilder.addCode(")");
    methodBuilder.addCode("$];\n");
    return Optional.of(methodBuilder.build());
  }
}
