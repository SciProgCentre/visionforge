plugins {
    id("scientifik.js")
}

kotlin {
    target {
        useCommonJs()
    }
}


dependencies{
    api(project(":visionforge-core"))

    //api("org.jetbrains:kotlin-react:16.13.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:16.13.1-pre.104-kotlin-1.3.72")

    api(npm("react", "16.13.1"))
    api(npm("react-dom", "16.13.1"))
    api(npm("react-is", "16.13.1"))
}