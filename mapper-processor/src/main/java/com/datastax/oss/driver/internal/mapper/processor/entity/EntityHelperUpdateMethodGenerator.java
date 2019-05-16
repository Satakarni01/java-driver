package com.datastax.oss.driver.internal.mapper.processor.entity;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;
import com.datastax.oss.driver.api.querybuilder.update.UpdateWithAssignments;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.squareup.javapoet.MethodSpec;
import java.util.Optional;
import javax.lang.model.element.Modifier;

public class EntityHelperUpdateMethodGenerator implements MethodGenerator {

  private final EntityDefinition entityDefinition;

  public EntityHelperUpdateMethodGenerator(
      EntityDefinition entityDefinition,
      EntityHelperGenerator enclosingClass,
      ProcessorContext context) {
    this.entityDefinition = entityDefinition;
  }

  @Override
  public Optional<MethodSpec> generate() {
    MethodSpec.Builder insertBuilder =
        MethodSpec.methodBuilder("update")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(UpdateWithAssignments.class)
            .addStatement("$T keyspaceId = context.getKeyspaceId()", CqlIdentifier.class)
            .addStatement("$T tableId = context.getTableId()", CqlIdentifier.class)
            .beginControlFlow("if (tableId == null)")
            .addStatement("tableId = DEFAULT_TABLE_ID")
            .endControlFlow()
            .addStatement(
                "$1T update = (keyspaceId == null)\n"
                    + "? $2T.update(tableId)\n"
                    + ": $2T.update(keyspaceId, tableId)",
                UpdateStart.class,
                QueryBuilder.class)
            .addCode("$[return update");

    for (PropertyDefinition property : entityDefinition.getAllColumns()) {
      insertBuilder.addCode(
          "\n.setColumn($1S, $2T.bindMarker($1S))", property.getCqlName(), QueryBuilder.class);
    }
    insertBuilder.addCode("$];\n");
    return Optional.of(insertBuilder.build());
  }
}
