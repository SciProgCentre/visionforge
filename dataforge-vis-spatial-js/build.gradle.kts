import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("org.jetbrains.kotlin.frontend")
}


val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(project(":dataforge-vis-spatial"))
    //implementation("ch.viseon.threejs:wrapper:105.0.0")
    implementation("info.laht.threekt:threejs-wrapper:0.88-npm-2")
    testCompile(kotlin("test-js"))
}

configure<KotlinFrontendExtension> {
    downloadNodeJsVersion = "latest"

    configure<NpmExtension> {
        dependency("three-full")
        dependency("style-loader")
        devDependency("karma")
    }

    sourceMaps = true

    bundle<WebPackExtension>("webpack") {
        this as WebPackExtension
        bundleName = "main"
        contentPath = file("src/main/web")
        sourceMapEnabled = true
        //mode = "production"
        mode = "development"
    }
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            metaInfo = true
            outputFile = "${project.buildDir.path}/js/${project.name}.js"
            sourceMap = true
            moduleKind = "commonjs"
            main = "call"
            kotlinOptions.sourceMapEmbedSources = "always"
        }
    }

    "compileTestKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            metaInfo = true
            outputFile = "${project.buildDir.path}/js/${project.name}-test.js"
            sourceMap = true
            moduleKind = "commonjs"
            kotlinOptions.sourceMapEmbedSources = "always"
        }
    }
}