@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

kotlin {

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets{
        wasmJsMain{
            dependencies{
                implementation(projects.plotlyKt.plotlyKtCore)
                implementation(spclibs.kotlinx.coroutines.core)
            }
        }
    }
}