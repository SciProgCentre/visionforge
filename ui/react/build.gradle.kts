plugins {
    id("scientifik.js")
}

kotlin {
    target {
        useCommonJs()
    }
}

val reactVersion by extra("16.13.1")

dependencies{
    api(project(":visionforge-core"))

    //api("org.jetbrains:kotlin-react:16.13.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:$reactVersion-pre.104-kotlin-1.3.72")

    api(npm("react", reactVersion))
    api(npm("react-dom", reactVersion))
    api(npm("react-is", reactVersion))
}