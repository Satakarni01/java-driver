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
package com.datastax.oss.driver.internal.core.session.throttling;

import java.io.Closeable;

/** Limits the number of concurrent requests executed by the driver. */
public interface RequestThrottler extends Closeable {

  /**
   * Registers a new request to be throttled. The throttler will invoke {@link
   * Throttled#onThrottleReady()} when the request is allowed to proceed.
   */
  void register(Throttled request);

  /**
   * Signals that a request has succeeded. This indicates to the throttler that another request
   * might be started.
   */
  void signalSuccess(Throttled request);

  /**
   * Signals that a request has failed. This indicates to the throttler that another request might
   * be started.
   */
  void signalError(Throttled request, Throwable error);

  /**
   * Signals that a request has timed out. This indicates to the throttler that this request has
   * stopped (if it was running already), or that it doesn't need to be started in the future.
   *
   * <p>Note: requests are responsible for handling their own timeout. The throttler does not
   * perform time-based eviction on pending requests.
   */
  void signalTimeout(Throttled request);
}