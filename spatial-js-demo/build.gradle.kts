plugins {
    id("scientifik.js")
    //id("kotlin-dce-js")
}

dependencies {
    api(project(":dataforge-vis-spatial"))
    testImplementation(kotlin("test-js"))
}

//kotlin{
//    target {
//        browser{
//            webpackTask {
//                sourceMaps = false
//            }
//        }
//    }
//}