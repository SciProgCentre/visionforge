import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("org.jetbrains.kotlin.frontend")
}

repositories {
    maven("https://kotlin.bintray.com/kotlin-js-wrappers")
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":dataforge-vis-common"))
    implementation("org.jetbrains:kotlin-react:16.6.0-pre.73-kotlin-1.3.40")
    implementation("org.jetbrains:kotlin-react-dom:16.6.0-pre.73-kotlin-1.3.40")
    testCompile(kotlin("test-js"))
}

configure<KotlinFrontendExtension> {
    downloadNodeJsVersion = "latest"

    configure<NpmExtension> {
        dependency("core-js", "3.1.4")
        dependency("cp-react-tree-table","1.0.0-beta.6")
        dependency("react")
        dependency("react-dom")
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
