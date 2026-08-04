plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":libraries:wayfarer-api"))
    implementation(project(":libraries:wayfarer-common"))
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.mariadb.client)
    testImplementation(project(":libraries:wayfarer-api"))
    testImplementation(libs.paper.api)
}

val mariaDbIntegrationTestSourceSet = sourceSets.create("mariaDbIntegrationTest")

configurations.named(mariaDbIntegrationTestSourceSet.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(mariaDbIntegrationTestSourceSet.runtimeOnlyConfigurationName) {
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
        mariaDbIntegrationTestSourceSet.implementationConfigurationName,
        project(":plugins:wayfarer-core")
    )
    add(mariaDbIntegrationTestSourceSet.implementationConfigurationName, libs.flyway.core)
    add(mariaDbIntegrationTestSourceSet.implementationConfigurationName, libs.flyway.mysql)
    add(mariaDbIntegrationTestSourceSet.implementationConfigurationName, libs.mariadb.client)
    add(mariaDbIntegrationTestSourceSet.implementationConfigurationName, libs.testcontainers.junit)
}

val mariaDbIntegrationTest = tasks.register<Test>("mariaDbIntegrationTest") {
    description = "Runs isolated Main migration integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(":plugins:wayfarer-core:shadowJar")
    testClassesDirs = mariaDbIntegrationTestSourceSet.output.classesDirs
    classpath = mariaDbIntegrationTestSourceSet.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.check {
    dependsOn(mariaDbIntegrationTest)
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
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
