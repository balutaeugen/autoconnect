package net.m1dLL3.client;

abstract class AutoConnectConfigBase {
    public static final int MAX_RETRY_COUNT = 99;
    public static final int MAX_RETRY_DELAY_SECONDS = 300;

    public boolean enabled = true;
    public String serverAddress = "";
    public String lastServerAddress = "";
    public boolean retryOnFailure = false;
    public int retryCount = 0;
    public int retryDelaySeconds = 3;

    public abstract void save();

    public void rememberServer(String address) {
        if (!AutoConnectServerAddress.isUsable(address)) {
            return;
        }

        String trimmedAddress = AutoConnectServerAddress.normalize(address);
        if (trimmedAddress.equals(lastServerAddress)) {
            return;
        }

        lastServerAddress = trimmedAddress;
        save();
    }

    public void useServerForAutoConnect(String address) {
        if (!AutoConnectServerAddress.isUsable(address)) {
            return;
        }

        String trimmedAddress = AutoConnectServerAddress.normalize(address);
        if (trimmedAddress.equals(serverAddress) && trimmedAddress.equals(lastServerAddress)) {
            return;
        }

        lastServerAddress = trimmedAddress;
        serverAddress = trimmedAddress;
        save();
    }

    public String connectAddress() {
        return AutoConnectServerAddress.normalize(serverAddress);
    }

    boolean sanitize() {
        boolean changed = false;
        if (serverAddress == null) {
            serverAddress = "";
            changed = true;
        } else {
            String sanitizedServerAddress = AutoConnectServerAddress.normalize(serverAddress);
            if (!serverAddress.equals(sanitizedServerAddress)) {
                serverAddress = sanitizedServerAddress;
                changed = true;
            }
        }
        if (lastServerAddress == null) {
            lastServerAddress = "";
            changed = true;
        } else {
            String sanitizedLastServerAddress = AutoConnectServerAddress.normalize(lastServerAddress);
            if (!lastServerAddress.equals(sanitizedLastServerAddress)) {
                lastServerAddress = sanitizedLastServerAddress;
                changed = true;
            }
        }

        int sanitizedRetryCount = Math.max(0, Math.min(MAX_RETRY_COUNT, retryCount));
        if (retryCount != sanitizedRetryCount) {
            retryCount = sanitizedRetryCount;
            changed = true;
        }

        int sanitizedRetryDelaySeconds = Math.max(0, Math.min(MAX_RETRY_DELAY_SECONDS, retryDelaySeconds));
        if (retryDelaySeconds != sanitizedRetryDelaySeconds) {
            retryDelaySeconds = sanitizedRetryDelaySeconds;
            changed = true;
        }

        return changed;
    }
}
