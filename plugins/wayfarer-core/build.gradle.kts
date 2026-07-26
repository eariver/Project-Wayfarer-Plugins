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
    mergeServiceFiles()
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
