package io.github.eariver.wayfarer.testkit;

import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

public final class MariaDbContainerFixture implements AutoCloseable {
    public static final String IMAGE = "mariadb:11.8";

    private final MariaDBContainer container;

    private MariaDbContainerFixture(MariaDBContainer container) {
        this.container = container;
    }

    public static MariaDbContainerFixture start() {
        String database = "wayfarer_" + UUID.randomUUID().toString().replace("-", "");
        MariaDBContainer container = new MariaDBContainer(DockerImageName.parse(IMAGE))
            .withDatabaseName(database)
            .withUsername("wayfarer_test")
            .withPassword("wayfarer_test_password");
        container.start();
        return new MariaDbContainerFixture(container);
    }

    public String jdbcUrl() {
        return container.getJdbcUrl();
    }

    public String username() {
        return container.getUsername();
    }

    public String password() {
        return container.getPassword();
    }

    public String databaseName() {
        return container.getDatabaseName();
    }

    @Override
    public void close() {
        container.close();
    }
}
