package com.tus.oss.server.impl;

import com.google.gson.Gson;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.tus.oss.server.core.UploadInfo;
import com.tus.oss.server.core.UploadManager;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tus.oss.server.core.Utils.parseMetadata;

/**
 * @author ckaratza
 * A redis backed-up implementation of the {@link UploadManager} interface. It uses redis getset to acquire an upload lock during the patch operation.
 * It delegates to an appropriate storage plugin to finish the persistence operation. Implementors could change the delegation with a more sophisticated
 * delegation decision if multiple storage plugins are involved based on metadata information.
 */
public class RedisUploadManager implements UploadManager {

    private static final Logger log = LoggerFactory.getLogger(RedisUploadManager.class);
    private static final String LOCK_PREFIX = "LPRUM_";
    private final StatefulRedisConnection<String, String> connection;
    private final String basePath;
    private final String contextPath;
    private final Long tusMaxSize;
    private final Gson gson;
    private final StoragePlugin storagePlugin;

    public RedisUploadManager(RedisClient redisClient, Long tusMaxSize, String basePath, String contextPath, StoragePlugin storagePlugin) {
        this.tusMaxSize = tusMaxSize;
        this.basePath = basePath;
        this.contextPath = contextPath;
        connection = redisClient.connect();
        this.storagePlugin = storagePlugin;
        gson = new Gson();
    }

    private Optional<UploadInfo> get(String id) {
        String value = connection.sync().get(id);
        if (value == null) return Optional.empty();
        return Optional.of(gson.fromJson(value, UploadInfo.class));
    }

    private void set(String id, UploadInfo info) {
        connection.sync().set(id, gson.toJson(info));
    }

    private boolean del(String id) {
        Long keyDeleted = connection.sync().del(id);
        return keyDeleted == 1;
    }

    @Override
    public Optional<UploadInfo> findUploadInfo(String id) {
        return get(id);
    }

    @Override
    public Optional<String> createUpload(Long totalLength, Optional<String> uploadMetadata, boolean isPartial) {
        String id = UUID.randomUUID().toString();
        UploadInfo info = new UploadInfo();
        info.setCreationUrl(basePath + contextPath + id);
        info.setEntityLength(totalLength);
        info.setOffset(0);
        info.setPartial(isPartial);
        if (uploadMetadata.isPresent()) {
            if (parseMetadata(uploadMetadata.get()).size() > 0) {
                info.setMetadata(uploadMetadata.get());
            }
        }
        set(id, info);
        log.info("New Upload created {}.", info);
        return Optional.of(info.getCreationUrl());
    }

    @Override
    public Optional<String> mergePartialUploads(String[] ids, Optional<String> uploadMetadata) {
        List<UploadInfo> partials = new ArrayList<>();
        for (String id : ids) {
            Optional<UploadInfo> info = get(id);
            if (!info.isPresent()) {
                log.warn("Partial Upload not found {}.", id);
                return Optional.empty();
            }
            if (!info.get().isPartial()) {
                log.warn("Upload not partial {}.", id);
                return Optional.empty();
            }
            if (info.get().getOffset() != info.get().getEntityLength()) {
                log.warn("Partial {} not completed.", id);
                return Optional.empty();
            }
            partials.add(info.get());
        }
        String id = UUID.randomUUID().toString();
        UploadInfo info = new UploadInfo();
        info.setCreationUrl(basePath + contextPath + id);
        info.setEntityLength(partials.stream().map(UploadInfo::getEntityLength).reduce(0L, (x, y) -> x + y));
        info.setOffset(info.getEntityLength());
        info.setPartial(false);
        if (uploadMetadata.isPresent()) {
            if (parseMetadata(uploadMetadata.get()).size() > 0) {
                info.setMetadata(uploadMetadata.get());
            }
        }
        set(id, info);
        log.info("New Upload created {} from partial uploads {}.", info, ids);
        return Optional.of(info.getCreationUrl());
    }

    @Override
    public boolean checkServerSizeConstraint(Long totalLength) {
        return totalLength <= tusMaxSize;
    }

    @Override
    public boolean discardUpload(String id) {
        boolean deleted = del(id);
        log.info("Deleted {}.", deleted);
        return deleted;
    }

    @Override
    public boolean acquireLock(String id) {
        String oldValue = connection.sync().getset(LOCK_PREFIX + id, "1");
        return !"1".equals(oldValue);
    }

    @Override
    public void releaseLock(String id) {
        del(LOCK_PREFIX + id);
    }

    @Override
    public long delegateToStoragePlugin(HttpServerRequest request, String id, long offset, Optional<UploadInfo.ChecksumInfo> checksum) {
        Optional<UploadInfo> uploadInfo = get(id);
        if (uploadInfo.isPresent()) {
            long bytesStored = storagePlugin.delegateBytesToStorage(uploadInfo.get(), offset, checksum);
            long totalSoFar = uploadInfo.get().getOffset() + bytesStored;
            uploadInfo.get().setOffset(totalSoFar);
            set(id, uploadInfo.get());
            log.info("Persisted Upload with id {} to store from offset {} until {} for entity length {}.", id, offset,
                    uploadInfo.get().getOffset(), uploadInfo.get().getEntityLength());
            return totalSoFar;
        } else
            return -1;
    }

}
