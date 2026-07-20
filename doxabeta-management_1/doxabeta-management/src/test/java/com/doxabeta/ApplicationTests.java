package com.doxabeta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The simplest possible "does this app work at all" test.
 *
 * @SpringBootTest boots the ENTIRE application in a test JVM — every entity,
 * repository, service, controller, and the SecurityConfig — wired together
 * exactly like a real run, but against an in-memory H2 database (see
 * application.yml, which has no active profile by default). Because
 * SeedService implements CommandLineRunner, the CSV seed also runs during
 * this test's startup, so this single test indirectly exercises the seeding
 * logic too.
 *
 * If this test fails, it means the application context couldn't start at all
 * (e.g. a missing bean, a bad property reference, a broken JPA mapping) —
 * that's a more fundamental problem than any individual endpoint failing.
 */
@SpringBootTest
class ApplicationTests {

    /**
     * An empty test body is intentional: @SpringBootTest already does the
     * real work by attempting to start the application context before this
     * method even runs. If startup throws, the test fails before reaching
     * this line; if we get here, the app is wired correctly.
     */
    @Test
    void contextLoads() {
    }
}
