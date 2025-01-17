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

import alluxio.grpc.table.BinaryColumnStatsData;
import alluxio.grpc.table.BooleanColumnStatsData;
import alluxio.grpc.table.ColumnStatisticsData;
import alluxio.grpc.table.ColumnStatisticsInfo;
import alluxio.grpc.table.Date;
import alluxio.grpc.table.DateColumnStatsData;
import alluxio.grpc.table.Decimal;
import alluxio.grpc.table.DecimalColumnStatsData;
import alluxio.grpc.table.DoubleColumnStatsData;
import alluxio.grpc.table.HiveBucketProperty;
import alluxio.grpc.table.LongColumnStatsData;
import alluxio.grpc.table.Schema;
import alluxio.grpc.table.SortingColumn;
import alluxio.grpc.table.Storage;
import alluxio.grpc.table.StorageFormat;
import alluxio.grpc.table.StringColumnStatsData;
import alluxio.table.under.hive.util.PathTranslator;

import com.google.protobuf.ByteString;
import org.apache.hadoop.hive.metastore.api.ColumnStatisticsObj;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Order;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for hive types.
 */
public class HiveUtils {
  private HiveUtils() {} // prevent instantiation

  /**
   * @param hiveSchema the hive schema
   * @return the proto representation
   */
  public static Schema toProtoSchema(List<FieldSchema> hiveSchema) {
    Schema.Builder schemaBuilder = Schema.newBuilder();
    schemaBuilder.addAllCols(toProto(hiveSchema));
    return schemaBuilder.build();
  }

  /**
   * @param hiveSchema the hive schema
   * @return the proto representation
   */
  public static List<alluxio.grpc.table.FieldSchema> toProto(List<FieldSchema> hiveSchema) {
    List<alluxio.grpc.table.FieldSchema> list = new ArrayList<>();
    for (FieldSchema field : hiveSchema) {
      alluxio.grpc.table.FieldSchema aFieldSchema = alluxio.grpc.table.FieldSchema.newBuilder()
          .setName(field.getName())
          .setType(field.getType()) // does not support complex types now
          .setComment(field.getComment() != null ? field.getComment() : "")
          .build();
      list.add(aFieldSchema);
    }
    return list;
  }

  /**
   * Convert from a StorageDescriptor to a Storage object.
   *
   * @param sd storage descriptor
   * @param translator path translator
   * @return storage proto object
   */
  public static Storage toProto(StorageDescriptor sd, PathTranslator translator)
      throws IOException {
    if (sd == null) {
      return Storage.getDefaultInstance();
    }
    String serDe = sd.getSerdeInfo() == null ? ""
        : sd.getSerdeInfo().getSerializationLib();
    StorageFormat format = StorageFormat.newBuilder()
        .setInputFormat(sd.getInputFormat())
        .setOutputFormat(sd.getOutputFormat())
        .setSerDe(serDe).build(); // Check SerDe info
    Storage.Builder storageBuilder = Storage.newBuilder();
    List<Order> orderList = sd.getSortCols();
    List<SortingColumn> sortingColumns;
    if (orderList == null) {
      sortingColumns = Collections.emptyList();
    } else {
      sortingColumns = orderList.stream().map(
          order -> SortingColumn.newBuilder().setColumnName(order.getCol())
              .setOrder(order.getOrder() == 1 ? SortingColumn.SortingOrder.ASCENDING
                  : SortingColumn.SortingOrder.DESCENDING).build())
          .collect(Collectors.toList());
    }
    return storageBuilder.setStorageFormat(format)
        .setLocation(translator.toAlluxioPath(sd.getLocation()))
        .setBucketProperty(HiveBucketProperty.newBuilder().setBucketCount(sd.getNumBuckets())
            .addAllBucketedBy(sd.getBucketCols()).addAllSortedBy(sortingColumns).build())
        .setSkewed(sd.getSkewedInfo() != null && (sd.getSkewedInfo().getSkewedColNames()) != null
            && !sd.getSkewedInfo().getSkewedColNames().isEmpty())
        .putAllSerdeParameters(sd.getParameters()).build();
  }

  /**
   * Convert ColumnStatisticsObj to proto definition.
   *
   * @param colStats column statistics
   * @return the proto form
   */
  public static ColumnStatisticsInfo toProto(ColumnStatisticsObj colStats) {
    ColumnStatisticsInfo.Builder builder = ColumnStatisticsInfo.newBuilder();
    builder.setColName(colStats.getColName()).setColType(colStats.getColType());
    org.apache.hadoop.hive.metastore.api.ColumnStatisticsData statsData = colStats.getStatsData();
    if (statsData.isSetBooleanStats()) {
      org.apache.hadoop.hive.metastore.api.BooleanColumnStatsData data =
          statsData.getBooleanStats();
      if (data != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setBooleanStats(BooleanColumnStatsData.newBuilder()
                .setNumTrues(data.getNumTrues()).setNumFalses(data.getNumFalses())
                .setNumNulls(data.getNumNulls()).setBitVectors(data.getBitVectors())
                .build()).build());
      }
    }
    if (statsData.isSetDoubleStats()) {
      org.apache.hadoop.hive.metastore.api.DoubleColumnStatsData doubleStats =
          statsData.getDoubleStats();
      if (doubleStats != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setDoubleStats(DoubleColumnStatsData.newBuilder()
                .setNumDVs(doubleStats.getNumDVs()).setHighValue(doubleStats.getHighValue())
                .setLowValue(doubleStats.getLowValue()).setNumNulls(doubleStats.getNumNulls())
                .setBitVectors(doubleStats.getBitVectors()).build()).build());
      }
    }
    if (statsData.isSetLongStats()) {
      org.apache.hadoop.hive.metastore.api.LongColumnStatsData longData =
          statsData.getLongStats();
      if (longData != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setLongStats(LongColumnStatsData.newBuilder()
                .setNumDVs(longData.getNumDVs()).setHighValue(longData.getHighValue())
                .setLowValue(longData.getLowValue())
                .setNumNulls(longData.getNumNulls()).setBitVectors(longData.getBitVectors())
                .build()).build());
      }
    }
    if (statsData.isSetStringStats()) {
      org.apache.hadoop.hive.metastore.api.StringColumnStatsData stringData =
          statsData.getStringStats();
      if (stringData != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setStringStats(StringColumnStatsData.newBuilder()
                .setNumDVs(stringData.getNumDVs()).setAvgColLen(stringData.getAvgColLen())
                .setMaxColLen(stringData.getMaxColLen())
                .setNumNulls(stringData.getNumNulls()).setBitVectors(stringData.getBitVectors())
                .build()).build());
      }
    }
    if (statsData.isSetStringStats()) {
      org.apache.hadoop.hive.metastore.api.BinaryColumnStatsData data =
          statsData.getBinaryStats();
      if (data != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setBinaryStats(BinaryColumnStatsData.newBuilder()
                .setMaxColLen(data.getMaxColLen()).setAvgColLen(data.getAvgColLen())
                .setNumNulls(data.getNumNulls()).setBitVectors(data.getBitVectors())
                .build()).build());
      }
    }
    if (statsData.isSetDateStats()) {
      org.apache.hadoop.hive.metastore.api.DateColumnStatsData data =
          statsData.getDateStats();
      if (data != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setDateStats(DateColumnStatsData.newBuilder()
                .setHighValue(toProto(data.getHighValue()))
                .setLowValue(toProto(data.getLowValue()))
                .setNumNulls(data.getNumNulls())
                .setNumDVs(data.getNumDVs())
                .setBitVectors(data.getBitVectors())
                .build()).build());
      }
    }

    if (statsData.isSetDecimalStats()) {
      org.apache.hadoop.hive.metastore.api.DecimalColumnStatsData data =
          statsData.getDecimalStats();
      if (data != null) {
        builder.setData(ColumnStatisticsData.newBuilder()
            .setDecimalStats(DecimalColumnStatsData.newBuilder()
                .setHighValue(toProto(data.getHighValue()))
                .setLowValue(toProto(data.getLowValue()))
                .setNumNulls(data.getNumNulls())
                .setNumDVs(data.getNumDVs())
                .setBitVectors(data.getBitVectors())
                .build()).build());
      }
    }
    return builder.build();
  }

  private static Date toProto(org.apache.hadoop.hive.metastore.api.Date date) {
    return Date.newBuilder().setDaysSinceEpoch(date.getDaysSinceEpoch()).build();
  }

  private static Decimal toProto(org.apache.hadoop.hive.metastore.api.Decimal decimal) {
    return Decimal.newBuilder().setScale(decimal.getScale())
        .setUnscaled(ByteString.copyFrom(decimal.getUnscaled())).build();
  }
}
