// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: grpc/table/table_master.proto

package alluxio.grpc.table;

public interface DoubleColumnStatsDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:alluxio.grpc.table.DoubleColumnStatsData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional double lowValue = 1;</code>
   */
  boolean hasLowValue();
  /**
   * <code>optional double lowValue = 1;</code>
   */
  double getLowValue();

  /**
   * <code>optional double highValue = 2;</code>
   */
  boolean hasHighValue();
  /**
   * <code>optional double highValue = 2;</code>
   */
  double getHighValue();

  /**
   * <code>optional int64 numNulls = 3;</code>
   */
  boolean hasNumNulls();
  /**
   * <code>optional int64 numNulls = 3;</code>
   */
  long getNumNulls();

  /**
   * <code>optional int64 numDVs = 4;</code>
   */
  boolean hasNumDVs();
  /**
   * <code>optional int64 numDVs = 4;</code>
   */
  long getNumDVs();

  /**
   * <code>optional string bitVectors = 5;</code>
   */
  boolean hasBitVectors();
  /**
   * <code>optional string bitVectors = 5;</code>
   */
  java.lang.String getBitVectors();
  /**
   * <code>optional string bitVectors = 5;</code>
   */
  com.google.protobuf.ByteString
      getBitVectorsBytes();
}
