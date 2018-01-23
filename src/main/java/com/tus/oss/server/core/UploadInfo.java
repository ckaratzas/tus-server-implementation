package com.tus.oss.server.core;

/**
 * @author ckaratza
 */
public class UploadInfo {
    private long entityLength;
    private long offset;
    private String metadata;
    private String creationUrl;
    private boolean isPartial;
    private String uploadConcatMergedValue;

    public long getEntityLength() {
        return entityLength;
    }

    public void setEntityLength(long entityLength) {
        this.entityLength = entityLength;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCreationUrl() {
        return creationUrl;
    }

    public void setCreationUrl(String creationUrl) {
        this.creationUrl = creationUrl;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    public String getUploadConcatMergedValue() {
        return uploadConcatMergedValue;
    }

    public void setUploadConcatMergedValue(String uploadConcatMergedValue) {
        this.uploadConcatMergedValue = uploadConcatMergedValue;
    }

    @Override
    public String toString() {
        return "UploadInfo{" +
                "entityLength=" + entityLength +
                ", offset=" + offset +
                ", metadata='" + metadata + '\'' +
                ", creationUrl='" + creationUrl + '\'' +
                ", isPartial=" + isPartial +
                '}';
    }

    public static class ChecksumInfo {
        private final String algorithm;
        private final String value;

        ChecksumInfo(String algorithm, String value) {
            this.algorithm = algorithm;
            this.value = value;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getValue() {
            return value;
        }
    }
}
