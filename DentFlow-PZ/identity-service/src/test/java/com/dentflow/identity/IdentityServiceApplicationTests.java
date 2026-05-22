package com.dentflow.identity;

import org.junit.jupiter.api.Test;

/**
 * Placeholder smoke test - the real application context is tested
 * via the PostgreSQL-backed CI pipeline.
 * This class intentionally does NOT boot Spring to avoid conflicts
 * between test-profile H2 config and CI environment variables.
 */
class IdentityServiceApplicationTests {

    @Test
    void placeholderTest() {
        // no-op: actual logic tested in AuthServiceTest
    }

}
