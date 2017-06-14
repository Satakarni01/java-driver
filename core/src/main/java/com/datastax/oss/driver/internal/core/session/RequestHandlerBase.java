/*
 * Copyright (C) 2017-2017 DataStax Inc.
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
package com.datastax.oss.driver.internal.core.session;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.config.CoreDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigProfile;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import java.util.Queue;

/** Factors code that should be common to most request handler implementations. */
public abstract class RequestHandlerBase<SyncResultT, AsyncResultT>
    implements RequestHandler<SyncResultT, AsyncResultT> {

  protected final Request<SyncResultT, AsyncResultT> request;
  protected final boolean isIdempotent;
  protected final DefaultSession session;
  protected final CqlIdentifier keyspace;
  protected final InternalDriverContext context;
  protected final Queue<Node> queryPlan;
  protected final DriverConfigProfile configProfile;

  protected RequestHandlerBase(
      Request<SyncResultT, AsyncResultT> request,
      DefaultSession session,
      InternalDriverContext context) {
    this.request = request;
    this.session = session;
    this.keyspace = session.getKeyspace();
    this.context = context;
    this.queryPlan = context.loadBalancingPolicyWrapper().newQueryPlan();

    if (request.getConfigProfile() != null) {
      this.configProfile = request.getConfigProfile();
    } else {
      DriverConfig config = context.config();
      String profileName = request.getConfigProfileName();
      this.configProfile =
          (profileName == null || profileName.isEmpty())
              ? config.defaultProfile()
              : config.getProfile(profileName);
    }
    this.isIdempotent =
        (request.isIdempotent() == null)
            ? configProfile.getBoolean(CoreDriverOption.REQUEST_DEFAULT_IDEMPOTENCE)
            : request.isIdempotent();
  }
}
