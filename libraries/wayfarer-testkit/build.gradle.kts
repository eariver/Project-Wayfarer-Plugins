plugins {
    `java-library`
}

dependencies {
    api(project(":libraries:wayfarer-api"))
    api(libs.testcontainers.junit)
    api(libs.testcontainers.mariadb)
    api(libs.mariadb.client)
}
