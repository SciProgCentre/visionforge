plugins {
    kotlin("jvm")
    kotlin("jupyter.api")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    implementation(project(":demo:playground"))
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = ru.mipt.npm.gradle.KScienceVersions.JVM_TARGET.toString()
    }
}

extensions.findByType<JavaPluginExtension>()?.apply {
    targetCompatibility = ru.mipt.npm.gradle.KScienceVersions.JVM_TARGET
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("playground.VisionForgePlayGroundForJupyter")
}

tasks.findByName("shadowJar")?.dependsOn("processJupyterApiResources")