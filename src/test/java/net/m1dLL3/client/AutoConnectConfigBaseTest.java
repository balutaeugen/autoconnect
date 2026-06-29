package net.m1dLL3.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoConnectConfigBaseTest {
    @Test
    void sanitizeNormalizesStringsAndClampsRetryValues() {
        TestConfig config = new TestConfig();
        config.serverAddress = "  example.org:25565  ";
        config.lastServerAddress = null;
        config.retryCount = -3;
        config.retryDelaySeconds = AutoConnectConfigBase.MAX_RETRY_DELAY_SECONDS + 1;
        config.configVersion = 0;

        assertTrue(config.sanitize());
        assertEquals(AutoConnectConfigBase.CURRENT_CONFIG_VERSION, config.configVersion);
        assertEquals("example.org:25565", config.serverAddress);
        assertEquals("", config.lastServerAddress);
        assertEquals(0, config.retryCount);
        assertEquals(AutoConnectConfigBase.MAX_RETRY_DELAY_SECONDS, config.retryDelaySeconds);
    }

    @Test
    void addressMemoryIgnoresBlankAndInvalidAddresses() {
        TestConfig config = new TestConfig();

        config.useServerForAutoConnect(" ");
        config.useServerForAutoConnect("bad address");
        config.rememberServer("localhost:99999");

        assertEquals("", config.serverAddress);
        assertEquals("", config.lastServerAddress);
        assertEquals(0, config.saveCount);
    }

    @Test
    void addressMemorySavesNormalizedValidAddresses() {
        TestConfig config = new TestConfig();

        config.useServerForAutoConnect("  play.example.org:25565 ");

        assertEquals("play.example.org:25565", config.serverAddress);
        assertEquals("play.example.org:25565", config.lastServerAddress);
        assertEquals(1, config.saveCount);

        config.rememberServer(" play.example.org:25565 ");
        assertEquals(1, config.saveCount);
    }

    @Test
    void connectAddressFallsBackToLastServerWhenSavedServerIsBlank() {
        TestConfig config = new TestConfig();
        config.serverAddress = " ";
        config.lastServerAddress = " play.example.org:25565 ";

        assertEquals("play.example.org:25565", config.connectAddress());
    }

    @Test
    void savedServerControlsPromoteAndClearLastServer() {
        TestConfig config = new TestConfig();
        config.lastServerAddress = "play.example.org";

        config.useLastServerForAutoConnect();

        assertEquals("play.example.org", config.serverAddress);
        assertEquals("play.example.org", config.lastServerAddress);
        assertEquals(1, config.saveCount);

        config.clearLastServerAddress();

        assertEquals("", config.lastServerAddress);
        assertEquals(2, config.saveCount);
    }

    @Test
    void addressValidatorRejectsMalformedPorts() {
        assertTrue(AutoConnectServerAddress.isUsable("localhost"));
        assertTrue(AutoConnectServerAddress.isUsable("localhost:25565"));
        assertFalse(AutoConnectServerAddress.isUsable("localhost:"));
        assertFalse(AutoConnectServerAddress.isUsable("localhost:0"));
        assertFalse(AutoConnectServerAddress.isUsable("localhost:70000"));
        assertFalse(AutoConnectServerAddress.isUsable("local host"));
    }

    private static final class TestConfig extends AutoConnectConfigBase {
        private int saveCount;

        @Override
        public void save() {
            saveCount++;
        }
    }
}
