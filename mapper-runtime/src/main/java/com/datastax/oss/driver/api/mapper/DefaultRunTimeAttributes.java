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

public class DefaultRunTimeAttributes implements RuntimeAttributes {

  private DriverExecutionProfile profile = null;

  private boolean isIdempotent = false;

  private boolean isIdempotentSet = false;

  private int pageSize = -1;

  private ConsistencyLevel consitencyLevel = null;

  private ConsistencyLevel serialConsistencyLevel = null;

  private Duration timeout = null;

  private ByteBuffer routingKey = null;

  private String routingKeyspace = null;

  private boolean tracing = false;

  private boolean tracingSet = false;

  private ByteBuffer pagingState = null;

  private long timeStamp = -1;

  private Node node = null;

  private Map<String, ByteBuffer> customPayload = null;

  public static DefaultRunTimeAttributesBuilder builder() {
    return new DefaultRunTimeAttributesBuilder();
  }

  @Override
  public boolean isExecutionProfileSet() {
    return profile != null;
  }

  @Override
  public DriverExecutionProfile getExecutionProfile() {
    return profile;
  }

  @Override
  public boolean isIdempotentSet() {
    return isIdempotentSet;
  }

  @Override
  public boolean isIdempotent() {
    return isIdempotent;
  }

  @Override
  public boolean isPageSizeSet() {
    return pageSize != -1;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public boolean isConsistencyLevelSet() {
    return consitencyLevel != null;
  }

  @Override
  public ConsistencyLevel getConstencyLevel() {
    return consitencyLevel;
  }

  @Override
  public boolean isSerialConsistencyLevelSet() {
    return serialConsistencyLevel != null;
  }

  @Override
  public ConsistencyLevel getSerialConsistencyLevel() {
    return serialConsistencyLevel;
  }

  @Override
  public boolean isTimeoutSet() {
    return timeout != null;
  }

  @Override
  public Duration getTimeout() {
    return timeout;
  }

  @Override
  public boolean isRoutingKeySet() {
    return routingKey != null;
  }

  @Override
  public ByteBuffer getRoutingKey() {
    return routingKey;
  }

  @Override
  public boolean isRoutingKeyspaceSet() {
    return routingKeyspace != null;
  }

  @Override
  public String getRoutingKeyspace() {
    return routingKeyspace;
  }

  @Override
  public boolean isTracingSet() {
    return tracingSet;
  }

  @Override
  public boolean isTracing() {
    return tracing;
  }

  @Override
  public boolean isPagingStateSet() {
    return pagingState != null;
  }

  @Override
  public ByteBuffer getPagingState() {
    return pagingState;
  }

  @Override
  public boolean isTimeStampSet() {
    return timeStamp != -1;
  }

  @Override
  public long getTimestamp() {
    return timeStamp;
  }

  @Override
  public boolean isNodeSet() {
    return node != null;
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public boolean isCustomPayloadSet() {
    return customPayload != null;
  }

  @Override
  public Map<String, ByteBuffer> getCustomPayload() {
    return customPayload;
  }

  protected void setProfile(DriverExecutionProfile profile) {
    this.profile = profile;
  }

  protected void setIdempotent(boolean idempotent) {
    isIdempotentSet = true;
    isIdempotent = idempotent;
  }

  protected void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  protected void setConsistencyLevel(ConsistencyLevel consitencyLevel) {
    this.consitencyLevel = consitencyLevel;
  }

  protected void setSerialConsistencyLevel(ConsistencyLevel serialConsistencyLevel) {
    this.serialConsistencyLevel = serialConsistencyLevel;
  }

  protected void setTimeout(Duration timeout) {
    this.timeout = timeout;
  }

  protected void setRoutingKey(ByteBuffer routingKey) {
    this.routingKey = routingKey;
  }

  protected void setRoutingKeyspace(String routingKeyspace) {
    this.routingKeyspace = routingKeyspace;
  }

  protected void setTracing(boolean tracing) {
    this.tracingSet = true;
    this.tracing = tracing;
  }

  protected void setPagingState(ByteBuffer pagingState) {
    this.pagingState = pagingState;
  }

  protected void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  protected void setNode(Node node) {
    this.node = node;
  }

  protected void setCustomPayload(Map<String, ByteBuffer> customPayload) {
    this.customPayload = customPayload;
  }
}
