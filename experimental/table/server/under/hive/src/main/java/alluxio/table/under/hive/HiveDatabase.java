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

package alluxio.table.under.hive;

import alluxio.conf.PropertyKey;
import alluxio.conf.ServerConfiguration;
import alluxio.grpc.FileStatistics;
import alluxio.table.common.UdbTable;
import alluxio.table.common.UnderDatabase;
import alluxio.underfs.UnderFileSystem;
import alluxio.underfs.UnderFileSystemConfiguration;
import alluxio.util.URIUtils;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.iceberg.catalog.TableIdentifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Hive database implementation.
 */
public class HiveDatabase implements UnderDatabase {
  private static final String DEFAULT_DB_NAME = "default";

  private final HiveDataCatalog mCatalog;
  private final Hive mHive;

  private HiveDatabase(HiveDataCatalog catalog, Hive hive) {
    mCatalog = catalog;
    mHive = hive;
  }

  /**
   * Creates an instance of the Hive database UDB.
   *
   * @param configuration the configuration
   * @return the new instance
   */
  public static HiveDatabase create(Map<String, String> configuration) throws IOException {
    UnderFileSystem ufs;
    if (URIUtils.isLocalFilesystem(ServerConfiguration
        .get(PropertyKey.MASTER_MOUNT_TABLE_ROOT_UFS))) {
      ufs = UnderFileSystem.Factory
          .create("/", UnderFileSystemConfiguration.defaults(ServerConfiguration.global()));
    } else {
      ufs = UnderFileSystem.Factory.createForRoot(ServerConfiguration.global());
    }
    HiveDataCatalog catalog = new HiveDataCatalog(ufs);
    // TODO(gpang): get rid of creating db
    catalog.createDatabase(DEFAULT_DB_NAME);

    Hive hive;
    try {
      HiveConf conf = new HiveConf();
      // TODO(gpang): use configuration keys passed in
      conf.set("hive.metastore.uris", "thrift://localhost:9083");
      hive = Hive.get(conf);
    } catch (HiveException e) {
      throw new IOException("Failed to create hive client: " + e.getMessage(), e);
    }
    return new HiveDatabase(catalog, hive);
  }

  @Override
  public String getType() {
    return HiveDatabaseFactory.TYPE;
  }

  @Override
  public List<String> getTableNames() throws IOException {
    try {
      return mHive.getAllTables(DEFAULT_DB_NAME);
    } catch (HiveException e) {
      throw new IOException("Failed to get hive tables: " + e.getMessage(), e);
    }
  }

  @Override
  public UdbTable getTable(String tableName) throws IOException {
    try {
      Table table = mHive.getTable(DEFAULT_DB_NAME, tableName);
      return new HiveTable(tableName, HiveUtils.toProto(table.getAllCols()),
          table.getDataLocation().toString(), null);
    } catch (HiveException e) {
      throw new IOException("Failed to get table: " + tableName + " error: " + e.getMessage(), e);
    }
  }

  @Override
  public Map<String, FileStatistics> getStatistics(String dbName, String tableName)
      throws IOException {
    mCatalog.getTable(TableIdentifier.of(DEFAULT_DB_NAME, tableName));
    return null;
  }
}