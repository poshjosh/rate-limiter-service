package io.github.poshjosh.ratelimiter.raas.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public final class HostAddress {
    private HostAddress() { }

    private static String hostAddress;
    public static String get() {
        if (hostAddress != null) {
            return hostAddress;
        }
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
            hostAddress = "localhost";
        }
        return hostAddress;
    }
}
