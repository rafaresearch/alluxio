// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: grpc/table/table_master.proto

package alluxio.grpc.table;

public interface DatabaseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:alluxio.grpc.table.Database)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   */
  boolean hasName();
  /**
   * <code>optional string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional string description = 2;</code>
   */
  boolean hasDescription();
  /**
   * <code>optional string description = 2;</code>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 2;</code>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional string location = 3;</code>
   */
  boolean hasLocation();
  /**
   * <code>optional string location = 3;</code>
   */
  java.lang.String getLocation();
  /**
   * <code>optional string location = 3;</code>
   */
  com.google.protobuf.ByteString
      getLocationBytes();

  /**
   * <code>map&lt;string, string&gt; parameter = 4;</code>
   */
  int getParameterCount();
  /**
   * <code>map&lt;string, string&gt; parameter = 4;</code>
   */
  boolean containsParameter(
      java.lang.String key);
  /**
   * Use {@link #getParameterMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getParameter();
  /**
   * <code>map&lt;string, string&gt; parameter = 4;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getParameterMap();
  /**
   * <code>map&lt;string, string&gt; parameter = 4;</code>
   */

  java.lang.String getParameterOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; parameter = 4;</code>
   */

  java.lang.String getParameterOrThrow(
      java.lang.String key);
}
