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
package com.datastax.oss.driver.mapper.model.inventory;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Simple {
  @PartitionKey private UUID pk;
  private String data;

  public Simple() {}

  public Simple(UUID pk, String data) {
    this.pk = pk;
    this.data = data;
  }

  public UUID getPk() {
    return pk;
  }

  public String getData() {
    return data;
  }

  public void setPk(UUID pk) {

    this.pk = pk;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Simple simple = (Simple) o;
    return Objects.equals(pk, simple.pk) && Objects.equals(data, simple.data);
  }

  @Override
  public int hashCode() {

    return Objects.hash(pk, data);
  }

  @Override
  public String toString() {
    return "Simple{" + "pk=" + pk + ", data='" + data + '\'' + '}';
  }
}
