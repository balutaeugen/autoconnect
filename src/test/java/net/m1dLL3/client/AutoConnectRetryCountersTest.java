package net.m1dLL3.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoConnectRetryCountersTest {
    @Test
    void countersDoNotShareAttempts() {
        TestConfig config = new TestConfig();
        config.retryOnFailure = false;

        AutoConnectRetryCounters counters = new AutoConnectRetryCounters();
        counters.autoConnect().recordAttempt();

        assertFalse(counters.autoConnect().canAttempt(config));
        assertTrue(counters.reconnect().canAttempt(config));
        assertTrue(counters.manualJoin().canAttempt(config));
    }

    @Test
    void retryEnabledAllowsFirstAttemptPlusConfiguredRetries() {
        TestConfig config = new TestConfig();
        config.retryOnFailure = true;
        config.retryCount = 2;

        AutoConnectRetryCounters.AttemptCounter counter = new AutoConnectRetryCounters().autoConnect();

        assertTrue(counter.canAttempt(config));
        counter.recordAttempt();
        assertTrue(counter.canAttempt(config));
        counter.recordAttempt();
        assertTrue(counter.canAttempt(config));
        counter.recordAttempt();
        assertFalse(counter.canAttempt(config));
    }

    @Test
    void resetClearsEveryCounter() {
        TestConfig config = new TestConfig();
        config.retryOnFailure = false;

        AutoConnectRetryCounters counters = new AutoConnectRetryCounters();
        counters.autoConnect().recordAttempt();
        counters.reconnect().recordAttempt();
        counters.manualJoin().recordAttempt();

        counters.reset();

        assertTrue(counters.autoConnect().canAttempt(config));
        assertTrue(counters.reconnect().canAttempt(config));
        assertTrue(counters.manualJoin().canAttempt(config));
    }

    private static final class TestConfig extends AutoConnectConfigBase {
        @Override
        public void save() {
        }
    }
}
