plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":libraries:wayfarer-api"))
    implementation(project(":libraries:wayfarer-common"))
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.mariadb.client)
    implementation(libs.lettuce)
    testImplementation(libs.paper.api)
}

val mariaDbIntegrationTestSourceSet = sourceSets.create("mariaDbIntegrationTest")
val redisIntegrationTestSourceSet = sourceSets.create("redisIntegrationTest")

configurations.named(mariaDbIntegrationTestSourceSet.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(mariaDbIntegrationTestSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}
configurations.named(redisIntegrationTestSourceSet.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(redisIntegrationTestSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    add(
        mariaDbIntegrationTestSourceSet.implementationConfigurationName,
        sourceSets.main.get().output
    )
    add(
        mariaDbIntegrationTestSourceSet.implementationConfigurationName,
        project(":libraries:wayfarer-testkit")
    )
    add(
        redisIntegrationTestSourceSet.implementationConfigurationName,
        sourceSets.main.get().output
    )
    add(
        redisIntegrationTestSourceSet.implementationConfigurationName,
        project(":libraries:wayfarer-testkit")
    )
    add(
        redisIntegrationTestSourceSet.implementationConfigurationName,
        libs.testcontainers.junit
    )
}

val mariaDbIntegrationTest = tasks.register<Test>("mariaDbIntegrationTest") {
    description = "Runs isolated MariaDB persistence integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = mariaDbIntegrationTestSourceSet.output.classesDirs
    classpath = mariaDbIntegrationTestSourceSet.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

val redisIntegrationTest = tasks.register<Test>("redisIntegrationTest") {
    description = "Runs isolated Redis foundation integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = redisIntegrationTestSourceSet.output.classesDirs
    classpath = redisIntegrationTestSourceSet.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.check {
    dependsOn(mariaDbIntegrationTest)
    dependsOn(redisIntegrationTest)
}

tasks.processResources {
    val expansionProperties = mapOf(
        "version" to project.version.toString(),
    )

    inputs.properties(expansionProperties)

    filesMatching("plugin.yml") {
        expand(expansionProperties)
    }
}


tasks.shadowJar {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    append("META-INF/LICENSE")
    append("META-INF/LICENSE.txt")
    append("META-INF/NOTICE")
    append("META-INF/io.netty.versions.properties")
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
