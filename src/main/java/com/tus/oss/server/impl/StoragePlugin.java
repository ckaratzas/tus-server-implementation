package com.tus.oss.server.impl;

import com.tus.oss.server.core.UploadInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author ckaratza
 * Feed the storage plugin with all the upload information in order to decide where and how it will store the bytes.
 * Depending on the storage provider implementors can adjust the implementation.
 * For now just simulate that bytes were actually processed...
 */
@Component
class StoragePlugin {

    long delegateBytesToStorage(UploadInfo info, long fromOffset, Optional<UploadInfo.ChecksumInfo> checksum) {
        //Replace it with a storage implementation of your own.
        //Simulate IO operation...
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Stored until the end...
        return info.getEntityLength() - fromOffset;
    }
}
