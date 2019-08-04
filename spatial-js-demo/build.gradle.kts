plugins {
    id("scientifik.js")
    //id("kotlin-dce-js")
}

dependencies {
    api(project(":dataforge-vis-spatial"))
    api("info.laht.threekt:threejs-wrapper:0.106-npm-3")
    testCompile(kotlin("test-js"))
}

//kotlin{
//    sourceSets["main"].dependencies{
//        implementation(npm("three","0.106.2"))
//        implementation(npm("@hi-level/three-csg"))
//        implementation(npm("style-loader"))
//        implementation(npm("element-resize-event"))
//    }
//}
