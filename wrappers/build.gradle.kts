plugins {
    id("scientifik.js")
    //id("kotlin-dce-js")
}

dependencies {
    api(project(":dataforge-vis-common"))

    testCompile(kotlin("test-js"))
}

kotlin{
    sourceSets["main"].apply{
        dependencies{
            api(npm("style-loader"))
            api(npm("inspire-tree","6.0.1"))
            api(npm("inspire-tree-dom","4.0.6"))
            api(npm("jsoneditor"))
            api(npm("dat.gui"))
            //api("org.jetbrains:kotlin-extensions:1.0.1-pre.83-kotlin-1.3.50")
        }
    }
}
