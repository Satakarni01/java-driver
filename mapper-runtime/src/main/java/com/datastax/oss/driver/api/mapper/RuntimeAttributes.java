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

public interface RuntimeAttributes {

  boolean isExecutionProfileSet();

  DriverExecutionProfile getExecutionProfile();

  boolean isIdempotentSet();

  boolean isIdempotent();

  boolean isPageSizeSet();

  int getPageSize();

  boolean isConsistencyLevelSet();

  ConsistencyLevel getConstencyLevel();

  boolean isSerialConsistencyLevelSet();

  ConsistencyLevel getSerialConsistencyLevel();

  boolean isTimeoutSet();

  Duration getTimeout();

  boolean isRoutingKeySet();

  ByteBuffer getRoutingKey();

  boolean isRoutingKeyspaceSet();

  String getRoutingKeyspace();

  boolean isTracingSet();

  boolean isTracing();

  boolean isPagingStateSet();

  ByteBuffer getPagingState();

  boolean isTimeStampSet();

  long getTimestamp();

  boolean isNodeSet();

  Node getNode();

  boolean isCustomPayloadSet();

  Map<String, ByteBuffer> customPayload();
}
