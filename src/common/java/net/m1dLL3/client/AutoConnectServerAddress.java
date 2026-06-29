package net.m1dLL3.client;

import java.util.regex.Pattern;

final class AutoConnectServerAddress {
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final int MAX_PORT = 65535;
    private static final Pattern HOST_CHARACTER_PATTERN = Pattern.compile("[A-Za-z0-9._:-]+");

    private AutoConnectServerAddress() {
    }

    static String normalize(String address) {
        return address == null ? "" : address.trim();
    }

    static boolean isUsable(String address) {
        String normalized = normalize(address);
        return !normalized.isBlank() && isValid(normalized);
    }

    static boolean isValid(String address) {
        String normalized = normalize(address);
        if (normalized.isBlank() || normalized.length() > MAX_ADDRESS_LENGTH || normalized.contains(" ")) {
            return false;
        }

        if (!HOST_CHARACTER_PATTERN.matcher(normalized).matches()) {
            return false;
        }

        int portSeparator = normalized.lastIndexOf(':');
        if (portSeparator < 0 || normalized.indexOf(':') != portSeparator) {
            return true;
        }

        String host = normalized.substring(0, portSeparator);
        String port = normalized.substring(portSeparator + 1);
        if (host.isBlank() || port.isBlank()) {
            return false;
        }

        try {
            int parsedPort = Integer.parseInt(port);
            return parsedPort > 0 && parsedPort <= MAX_PORT;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
