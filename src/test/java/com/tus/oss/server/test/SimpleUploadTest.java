package com.tus.oss.server.test;

import io.tus.java.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author ckaratza
 * A simple upload example using a tus-java-client [https://github.com/tus/tus-java-client].
 */
public class SimpleUploadTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleUploadTest.class);

    public static void main(String[] args) throws IOException, ProtocolException {
        if (args.length != 2) {
            log.info("Usage: Supply 2 arguments-> 1. [TUS_SERVER_URL] 2. [FILE_TO_UPLOAD_PATH]");
            return;
        }

        TusClient client = new TusClient();
        client.setUploadCreationURL(new URL(args[0]));
        client.enableResuming(new TusURLMemoryStore());

        File file = new File(args[1]);
        final TusUpload upload = new TusUpload(file);
        log.info("Starting upload {}...", upload);
        TusExecutor executor = new TusExecutor() {
            @Override
            protected void makeAttempt() throws ProtocolException, IOException {
                TusUploader uploader = client.resumeOrCreateUpload(upload);
                uploader.setChunkSize(1024);
                do {
                    long totalBytes = upload.getSize();
                    long bytesUploaded = uploader.getOffset();
                    double progress = (double) bytesUploaded / totalBytes * 100;
                    log.info("Upload at {}%.\n", progress);
                } while (uploader.uploadChunk() > -1);
                uploader.finish();
                log.info("Upload finished.");
                log.info("Upload available at: {}.", uploader.getUploadURL().toString());
            }
        };
        executor.makeAttempts();
    }
}
