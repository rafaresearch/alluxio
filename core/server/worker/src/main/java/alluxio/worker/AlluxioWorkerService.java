/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.worker;

import alluxio.Server;
import alluxio.wire.WorkerNetAddress;
import alluxio.worker.block.BlockWorker;

import java.net.InetSocketAddress;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A worker in the Alluxio system.
 */
public interface AlluxioWorkerService extends Server {
  /**
   * Factory for creating {@link AlluxioWorkerService}.
   */
  @ThreadSafe
  final class Factory {
    /**
     * @return a new instance of {@link AlluxioWorkerService}
     */
    public static AlluxioWorkerService create() {
      return new DefaultAlluxioWorker();
    }

    private Factory() {} // prevent instantiation
  }

  /**
   * @return the connect information for this worker
   */
  WorkerNetAddress getAddress();

  /**
   * @return the block worker for this Alluxio worker
   */
  BlockWorker getBlockWorker();

  /**
   * @return the ufs manager for this Alluxio worker
   */
  WorkerUfsManager getUfsManager();

  /**
   * @return the worker's data service bind host (used by unit test only)
   */
  String getDataBindHost();

  /**
   * @return the worker's data service port (used by unit test only)
   */
  int getDataLocalPort();

  /**
   * @return this worker's rpc address
   */
  InetSocketAddress getRpcAddress();

  /**
   * @return the start time of the worker in milliseconds
   */
  long getStartTimeMs();

  /**
   * @return the uptime of the worker in milliseconds
   */
  long getUptimeMs();

  /**
   * @return the worker web service bind host (used by unit test only)
   */
  String getWebBindHost();

  /**
   * @return the worker web service port (used by unit test only)
   */
  int getWebLocalPort();

  /**
   * Waits until the worker is ready to server requests.
   */
  void waitForReady();
}
