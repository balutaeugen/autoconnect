package net.m1dLL3.client;

final class AutoConnectRetryCounters {
    private final AttemptCounter autoConnect = new AttemptCounter();
    private final AttemptCounter reconnect = new AttemptCounter();
    private final AttemptCounter manualJoin = new AttemptCounter();

    AttemptCounter autoConnect() {
        return autoConnect;
    }

    AttemptCounter reconnect() {
        return reconnect;
    }

    AttemptCounter manualJoin() {
        return manualJoin;
    }

    void reset() {
        autoConnect.reset();
        reconnect.reset();
        manualJoin.reset();
    }

    static final class AttemptCounter {
        private int attempts;

        int attempts() {
            return attempts;
        }

        void recordAttempt() {
            attempts++;
        }

        void reset() {
            attempts = 0;
        }

        boolean canAttempt(AutoConnectConfigBase config) {
            if (!config.retryOnFailure && attempts > 0) {
                return false;
            }

            return !config.retryOnFailure || attempts < 1 + config.retryCount;
        }
    }
}
