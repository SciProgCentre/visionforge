val dataforgeVersion by extra("0.1.3-dev-7")

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("http://npm.mipt.ru:8081/artifactory/gradle-dev-local")
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