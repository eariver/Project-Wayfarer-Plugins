plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":libraries:wayfarer-api"))
    compileOnly(libs.paper.api)
    compileOnly(libs.vault.api)
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

tasks.jar {
    archiveBaseName.set("wayfarer-concrete-waymark-probe")
}
