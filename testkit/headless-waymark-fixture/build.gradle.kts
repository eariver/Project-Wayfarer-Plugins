plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(libs.vault.api)
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
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set("wayfarer-preclient-waymark-fixture")
    archiveClassifier.set("")
}
