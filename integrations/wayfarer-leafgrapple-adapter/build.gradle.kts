plugins {
    `java-library`
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":libraries:wayfarer-api"))
    testImplementation(libs.paper.api)
}
