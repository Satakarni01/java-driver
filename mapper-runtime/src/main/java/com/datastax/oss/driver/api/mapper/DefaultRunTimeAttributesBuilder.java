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
package com.datastax.oss.driver.api.mapper;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.metadata.Node;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

public class DefaultRunTimeAttributesBuilder {
  DefaultRunTimeAttributes attributes;

  public DefaultRunTimeAttributesBuilder() {
    attributes = new DefaultRunTimeAttributes();
  }

  public DefaultRunTimeAttributesBuilder withExecutionProfile(DriverExecutionProfile profile) {
    attributes.setProfile(profile);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withIdempotent(boolean idempotent) {
    attributes.setIdempotent(idempotent);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withPageSize(int pageSize) {
    attributes.setPageSize(pageSize);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withConsistencyLevel(ConsistencyLevel level) {
    attributes.setConsistencyLevel(level);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withSerialConsistencyLevel(ConsistencyLevel level) {
    attributes.setSerialConsistencyLevel(level);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withTimeout(Duration timeout) {
    attributes.setTimeout(timeout);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withRoutingKey(ByteBuffer routingKey) {
    attributes.setRoutingKey(routingKey);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withRoutingKeyspace(String keyspace) {
    attributes.setRoutingKeyspace(keyspace);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withTracing(boolean tracing) {
    attributes.setTracing(tracing);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withPagingState(ByteBuffer pagingState) {
    attributes.setPagingState(pagingState);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withTimeStamp(long timeStamp) {
    attributes.setTimeStamp(timeStamp);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withNode(Node node) {
    attributes.setNode(node);
    return this;
  }

  public DefaultRunTimeAttributesBuilder withCustomPayload(Map<String, ByteBuffer> customPayload) {
    attributes.setCustomPayload(customPayload);
    return this;
  }

  public RuntimeAttributes build() {
    return attributes;
  }
}
