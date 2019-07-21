plugins {
    id("scientifik.js")
    id("kotlin-dce-js")
}

//val kotlinVersion: String by rootProject.extra

dependencies {
    api(project(":dataforge-vis-spatial"))
    api(project(":dataforge-vis-spatial-gdml"))
    api("info.laht.threekt:threejs-wrapper:0.106-npm-2")
    testCompile(kotlin("test-js"))
}

kotlin{
    sourceSets["main"].dependencies{
        api(npm("three","0.106.2"))
        implementation(npm("@hi-level/three-csg"))
        implementation(npm("style-loader"))
        implementation(npm("element-resize-event"))
    }
}

//
//configure<KotlinFrontendExtension> {
//    downloadNodeJsVersion = "latest"
//
//    configure<NpmExtension> {
//        dependency("three","0.106.2")
//        dependency("@hi-level/three-csg")
//        dependency("style-loader")
//        dependency("element-resize-event")
//        devDependency("karma")
//    }
//
//    sourceMaps = true
//
//    bundle<WebPackExtension>("webpack") {
//        this as WebPackExtension
//        bundleName = "main"
//        contentPath = file("src/main/web")
//        sourceMapEnabled = true
//        //mode = "production"
//        mode = "development"
//    }
//}
//
//tasks {
//    "compileKotlin2Js"(Kotlin2JsCompile::class) {
//        kotlinOptions {
//            metaInfo = true
//            outputFile = "${project.buildDir.path}/js/${project.name}.js"
//            sourceMap = true
//            moduleKind = "commonjs"
//            main = "call"
//            kotlinOptions.sourceMapEmbedSources = "always"
//        }
//    }
//
//    "compileTestKotlin2Js"(Kotlin2JsCompile::class) {
//        kotlinOptions {
//            metaInfo = true
//            outputFile = "${project.buildDir.path}/js/${project.name}-test.js"
//            sourceMap = true
//            moduleKind = "commonjs"
//            kotlinOptions.sourceMapEmbedSources = "always"
//        }
//    }
//}