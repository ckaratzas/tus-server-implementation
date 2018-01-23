package com.tus.oss.server.core;

import io.vertx.core.http.HttpServerRequest;

import java.util.Optional;

/**
 * @author ckaratza
 * UploadManager interface decouples protocol from storage implementation by managing the upload information and flow.
 * The {@code delegateToStoragePlugin} method delegates to the available storage plugin(s) to perform the actual persistance.
 */
public interface UploadManager {

    Optional<UploadInfo> findUploadInfo(final String id);

    Optional<String> createUpload(Long totalLength, Optional<String> uploadMetadata, boolean isPartial);

    Optional<String> mergePartialUploads(String[] ids, Optional<String> uploadMetadata);

    boolean checkServerSizeConstraint(final Long totalLength);

    boolean discardUpload(final String id);

    boolean acquireLock(final String id);

    void releaseLock(final String id);

    long delegateToStoragePlugin(HttpServerRequest request, final String id, long offset, Optional<UploadInfo.ChecksumInfo> checksum);
}
