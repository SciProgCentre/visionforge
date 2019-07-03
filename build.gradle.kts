val dataforgeVersion by extra("0.1.3-dev-7")

plugins{
    kotlin("jvm") version "1.3.40" apply false
    id("kotlin2js") version "1.3.40" apply false
    id("kotlin-dce-js") version "1.3.40" apply false
    id("org.jetbrains.kotlin.frontend") version "0.0.45" apply false
    id("scientifik.mpp") version "0.1.0" apply false
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("http://npm.mipt.ru:8081/artifactory/gradle-dev-local")
        maven("https://kotlin.bintray.com/js-externals")
    }

    group = "hep.dataforge"
    version = dataforgeVersion
}

subprojects {
//    apply(plugin = "dokka-publish")
//    if (name.startsWith("dataforge")) {
//        apply(plugin = "npm-publish")
//    }
}