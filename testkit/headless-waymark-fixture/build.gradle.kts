plugins {
    `java-library`
}

dependencies {
    compileOnly(libs.paper.api)
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
    archiveBaseName.set("wayfarer-preclient-waymark-fixture")
}
