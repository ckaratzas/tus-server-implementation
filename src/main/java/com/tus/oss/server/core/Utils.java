package com.tus.oss.server.core;

import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author ckaratza
 * Request Handling utility functions.
 */
public final class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static Map<String, String> parseMetadata(String metadata) {
        HashMap<String, String> map = new HashMap<>();
        if (metadata == null) {
            return map;
        }
        String[] pairs = metadata.split(",");
        for (String pair : pairs) {
            String[] element = pair.trim().split(" ");
            if (element.length != 2) {
                log.warn("Ignoring metadata element: {}.", pair);
                continue;
            }
            String key = element[0];
            byte[] value;
            try {
                value = Base64.getUrlDecoder().decode(element[1]);
            } catch (IllegalArgumentException iae) {
                log.warn("Invalid encoding of metadata element: {}.", pair);
                continue;
            }
            map.put(key, new String(value));
        }
        return map;
    }

    static Optional<Long> getHeaderAsLong(final String key, final RoutingContext ctx) {
        String value = ctx.request().getHeader(key);
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    static Optional<String> getHeaderAsString(final String key, final RoutingContext ctx) {
        String value = ctx.request().getHeader(key);
        return Optional.ofNullable(value);
    }

    static Optional<UploadInfo.ChecksumInfo> getHeaderAsChecksumInfo(final String key, RoutingContext ctx) {
        String value = ctx.request().getHeader(key);
        if (value == null) return Optional.empty();
        String[] pair = value.split(" ");
        if (pair.length == 2) {
            return Optional.of(new UploadInfo.ChecksumInfo(pair[0], pair[1]));
        }
        return Optional.empty();
    }

    static String[] extractPartialUploadIds(String[] fullParts) {
        return Arrays.stream(fullParts).map(Utils::getLastBitFromUrl).toArray(String[]::new);
    }

    private static String getLastBitFromUrl(final String url) {
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }
}
