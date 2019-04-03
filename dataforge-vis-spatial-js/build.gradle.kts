import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("org.jetbrains.kotlin.frontend")
}


val kotlinVersion: String by rootProject.extra

dependencies {
    api(project(":dataforge-vis-spatial"))
    implementation("info.laht.threekt:threejs-wrapper:0.88-npm-2")
    testCompile("org.jetbrains.kotlin:kotlin-test-js:$kotlinVersion")
}

configure<KotlinFrontendExtension> {
    downloadNodeJsVersion = "latest"

    configure<NpmExtension> {
        dependency("three-full")
        dependency("style-loader")
        devDependency("karma")
    }

    sourceMaps = true

    bundle("webpack") {
        this as WebPackExtension
        bundleName = "main"
        proxyUrl = "http://localhost:8080"
        contentPath = file("src/main/web")
        sourceMapEnabled = true
        //mode = "production"
        mode = "development"
    }
}

tasks{
    compileKotlin2Js{
        kotlinOptions{
            metaInfo = true
            outputFile = "${project.buildDir.path}/js/${project.name}.js"
            sourceMap = true
            moduleKind = "umd"
            main = "call"
            kotlinOptions.sourceMapEmbedSources = "always"
        }
    }

    compileTestKotlin2Js{
        kotlinOptions{
            metaInfo = true
            outputFile = "${project.buildDir.path}/js/${project.name}-test.js"
            sourceMap = true
            moduleKind = "umd"
            kotlinOptions.sourceMapEmbedSources = "always"
        }
    }
}