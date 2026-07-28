package io.github.eariver.wayfarer.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CoreDependencyBoundaryTest {
    @Test
    void coreRuntimeClasspathDoesNotContainMainOrFrontierImplementations() {
        ClassLoader loader = CoreRuntime.class.getClassLoader();
        assertThrows(
            ClassNotFoundException.class,
            () -> Class.forName(
                "io.github.eariver.wayfarer.main.WayfarerMainPlugin",
                false,
                loader
            )
        );
        assertThrows(
            ClassNotFoundException.class,
            () -> Class.forName(
                "io.github.eariver.wayfarer.frontier.WayfarerFrontierPlugin",
                false,
                loader
            )
        );
    }
}
