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

package alluxio.client.file.options;

import alluxio.client.block.policy.BlockLocationPolicy;
import alluxio.conf.AlluxioConfiguration;
import alluxio.conf.PropertyKey;
import alluxio.annotation.PublicApi;
import alluxio.client.AlluxioStorageType;
import alluxio.client.UnderStorageType;
import alluxio.client.WriteType;
import alluxio.grpc.CreateFilePOptions;
import alluxio.grpc.FileSystemMasterCommonPOptions;
import alluxio.security.authorization.AccessControlList;
import alluxio.security.authorization.Mode;
import alluxio.util.FileSystemOptions;
import alluxio.util.IdUtils;
import alluxio.util.ModeUtils;
import alluxio.util.SecurityUtils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Method options for writing a file.
 */
@PublicApi
@NotThreadSafe
public final class OutStreamOptions {
  private FileSystemMasterCommonPOptions mCommonOptions;
  private long mBlockSizeBytes;
  private BlockLocationPolicy mLocationPolicy;
  private int mWriteTier;
  private WriteType mWriteType;
  private String mOwner;
  private String mGroup;
  private Mode mMode;
  private AccessControlList mAcl;
  private int mReplicationDurable;
  private int mReplicationMax;
  private int mReplicationMin;
  private String mUfsPath;
  private long mMountId;

  /**
   * @param alluxioConf Alluxio configuration
   * @return the default {@link OutStreamOptions}
   */
  public static OutStreamOptions defaults(AlluxioConfiguration alluxioConf) {
    return new OutStreamOptions(alluxioConf);
  }

  /**
   * Creates an {@link OutStreamOptions} instance from given options.
   *
   * @param options CreateFile options
   * @param alluxioConf Alluxio configuration
   * @throws Exception if {@link BlockLocationPolicy} can't be loaded
   */
  public OutStreamOptions(CreateFilePOptions options, AlluxioConfiguration alluxioConf) {
    this(alluxioConf);
    if (options.hasCommonOptions()) {
      mCommonOptions = mCommonOptions.toBuilder().mergeFrom(options.getCommonOptions()).build();
    }
    if (options.hasBlockSizeBytes()) {
      mBlockSizeBytes = options.getBlockSizeBytes();
    }
    if (options.hasMode()) {
      mMode = Mode.fromProto(options.getMode());
    }
    if (options.hasReplicationDurable()) {
      mReplicationDurable = options.getReplicationDurable();
    }
    if (options.hasReplicationMin()) {
      mReplicationMin = options.getReplicationMin();
    }
    if (options.hasReplicationMax()) {
      mReplicationMax = options.getReplicationMax();
    }
    if (options.hasWriteTier()) {
      mWriteTier = options.getWriteTier();
    }
    if (options.hasWriteType()) {
      mWriteType = WriteType.fromProto(options.getWriteType());
    }
    try {
      mLocationPolicy = BlockLocationPolicy.Factory.create(
          alluxioConf.get(PropertyKey.USER_BLOCK_WRITE_LOCATION_POLICY), alluxioConf);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private OutStreamOptions(AlluxioConfiguration alluxioConf) {
    mCommonOptions = FileSystemOptions.commonDefaults(alluxioConf);
    mBlockSizeBytes = alluxioConf.getBytes(PropertyKey.USER_BLOCK_SIZE_BYTES_DEFAULT);
    mLocationPolicy = BlockLocationPolicy.Factory.create(
        alluxioConf.get(PropertyKey.USER_BLOCK_WRITE_LOCATION_POLICY),
        alluxioConf);
    mWriteTier = alluxioConf.getInt(PropertyKey.USER_FILE_WRITE_TIER_DEFAULT);
    mWriteType = alluxioConf.getEnum(PropertyKey.USER_FILE_WRITE_TYPE_DEFAULT, WriteType.class);
    mOwner = SecurityUtils.getOwnerFromLoginModule(alluxioConf);
    mGroup = SecurityUtils.getGroupFromLoginModule(alluxioConf);
    mMode = ModeUtils.applyFileUMask(Mode.defaults(), alluxioConf
        .get(PropertyKey.SECURITY_AUTHORIZATION_PERMISSION_UMASK));
    mMountId = IdUtils.INVALID_MOUNT_ID;
    mReplicationDurable = alluxioConf.getInt(PropertyKey.USER_FILE_REPLICATION_DURABLE);
    mReplicationMax = alluxioConf.getInt(PropertyKey.USER_FILE_REPLICATION_MAX);
    mReplicationMin = alluxioConf.getInt(PropertyKey.USER_FILE_REPLICATION_MIN);
  }

  /**
   * @return the acl
   */
  public AccessControlList getAcl() {
    return mAcl;
  }

  /**
   * @return the block size
   */
  public long getBlockSizeBytes() {
    return mBlockSizeBytes;
  }

  /**
   * @return the file write location policy
   */
  public BlockLocationPolicy getLocationPolicy() {
    return mLocationPolicy;
  }

  /**
   * @return the Alluxio storage type
   */
  public AlluxioStorageType getAlluxioStorageType() {
    return mWriteType.getAlluxioStorageType();
  }

  /**
   * @return the common options
   */
  public FileSystemMasterCommonPOptions getCommonOptions() {
    return mCommonOptions;
  }

  /**
   * @return the under storage type
   */
  public UnderStorageType getUnderStorageType() {
    return mWriteType.getUnderStorageType();
  }

  /**
   * @return the owner
   */
  public String getOwner() {
    return mOwner;
  }

  /**
   * @return the group
   */
  public String getGroup() {
    return mGroup;
  }

  /**
   * @return the mode
   */
  public Mode getMode() {
    return mMode;
  }

  /**
   * @return the number of block replication for durable write
   */
  public int getReplicationDurable() {
    return mReplicationDurable;
  }

  /**
   * @return the maximum number of block replication
   */
  public int getReplicationMax() {
    return mReplicationMax;
  }

  /**
   * @return the minimum number of block replication
   */
  public int getReplicationMin() {
    return mReplicationMin;
  }

  /**
   * @return the mount id
   */
  public long getMountId() {
    return mMountId;
  }

  /**
   * @return the ufs path
   */
  public String getUfsPath() {
    return mUfsPath;
  }

  /**
   * @return the write tier
   */
  public int getWriteTier() {
    return mWriteTier;
  }

  /**
   * @return the write type
   */
  public WriteType getWriteType() {
    return mWriteType;
  }

  /**
   * Sets the acl of the file.
   *
   * @param acl the acl to use
   * @return the updated options object
   */
  public OutStreamOptions setAcl(AccessControlList acl) {
    mAcl = acl;
    return this;
  }

  /**
   * Sets the size of the block in bytes.
   *
   * @param blockSizeBytes the block size to use
   * @return the updated options object
   */
  public OutStreamOptions setBlockSizeBytes(long blockSizeBytes) {
    mBlockSizeBytes = blockSizeBytes;
    return this;
  }

  /**
   * @param locationPolicy the file write location policy
   * @return the updated options object
   */
  public OutStreamOptions setLocationPolicy(BlockLocationPolicy locationPolicy) {
    mLocationPolicy = locationPolicy;
    return this;
  }

  /**
   * Sets the write tier.
   *
   * @param writeTier the write tier to use for this operation
   * @return the updated options object
   */
  public OutStreamOptions setWriteTier(int writeTier) {
    mWriteTier = writeTier;
    return this;
  }

  /**
   * Sets the {@link WriteType}.
   *
   * @param writeType the {@link WriteType} to use for this operation. This will override both the
   *        {@link AlluxioStorageType} and {@link UnderStorageType}.
   * @return the updated options object
   */
  public OutStreamOptions setWriteType(WriteType writeType) {
    mWriteType = writeType;
    return this;
  }

  /**
   * @param mountId the mount id
   * @return the updated options object
   */
  public OutStreamOptions setMountId(long mountId) {
    mMountId = mountId;
    return this;
  }

  /**
   * @param ufsPath the ufs path
   * @return the updated options object
   */
  public OutStreamOptions setUfsPath(String ufsPath) {
    mUfsPath = ufsPath;
    return this;
  }

  /**
   * @param owner the owner to set
   * @return the updated object
   */
  public OutStreamOptions setOwner(String owner) {
    mOwner = owner;
    return this;
  }

  /**
   * @param group the group to set
   * @return the updated object
   */
  public OutStreamOptions setGroup(String group) {
    mGroup = group;
    return this;
  }

  /**
   * @param replicationDurable the number of block replication for durable write
   * @return the updated options object
   */
  public OutStreamOptions setReplicationDurable(int replicationDurable) {
    mReplicationDurable = replicationDurable;
    return this;
  }

  /**
   * @param replicationMax the maximum number of block replication
   * @return the updated options object
   */
  public OutStreamOptions setReplicationMax(int replicationMax) {
    mReplicationMax = replicationMax;
    return this;
  }

  /**
   * @param replicationMin the minimum number of block replication
   * @return the updated options object
   */
  public OutStreamOptions setReplicationMin(int replicationMin) {
    mReplicationMin = replicationMin;
    return this;
  }

  /**
   * @param mode the permission
   * @return the updated options object
   */
  public OutStreamOptions setMode(Mode mode) {
    mMode = mode;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OutStreamOptions)) {
      return false;
    }
    OutStreamOptions that = (OutStreamOptions) o;
    return Objects.equal(mAcl, that.mAcl)
        && Objects.equal(mBlockSizeBytes, that.mBlockSizeBytes)
        && Objects.equal(mCommonOptions, that.mCommonOptions)
        && Objects.equal(mGroup, that.mGroup)
        && Objects.equal(mLocationPolicy, that.mLocationPolicy)
        && Objects.equal(mMode, that.mMode)
        && Objects.equal(mMountId, that.mMountId)
        && Objects.equal(mOwner, that.mOwner)
        && Objects.equal(mReplicationDurable, that.mReplicationDurable)
        && Objects.equal(mReplicationMax, that.mReplicationMax)
        && Objects.equal(mReplicationMin, that.mReplicationMin)
        && Objects.equal(mUfsPath, that.mUfsPath)
        && Objects.equal(mWriteTier, that.mWriteTier)
        && Objects.equal(mWriteType, that.mWriteType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        mAcl,
        mBlockSizeBytes,
        mCommonOptions,
        mGroup,
        mLocationPolicy,
        mMode,
        mMountId,
        mOwner,
        mReplicationDurable,
        mReplicationMax,
        mReplicationMin,
        mUfsPath,
        mWriteTier,
        mWriteType
    );
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("acl", mAcl)
        .add("blockSizeBytes", mBlockSizeBytes)
        .add("commonOptions", mCommonOptions)
        .add("group", mGroup)
        .add("locationPolicy", mLocationPolicy)
        .add("mode", mMode)
        .add("mountId", mMountId)
        .add("owner", mOwner)
        .add("ufsPath", mUfsPath)
        .add("writeTier", mWriteTier)
        .add("writeType", mWriteType)
        .add("replicationDurable", mReplicationDurable)
        .add("replicationMax", mReplicationMax)
        .add("replicationMin", mReplicationMin)
        .toString();
  }
}