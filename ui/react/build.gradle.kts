plugins {
    id("ru.mipt.npm.js")
}

val reactVersion by extra("17.0.0")

dependencies{
    api(project(":visionforge-core"))

    //api("org.jetbrains:kotlin-react:16.13.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:$reactVersion-pre.126-kotlin-1.4.10")

    api(npm("react", reactVersion))
    api(npm("react-dom", reactVersion))
    api(npm("react-is", reactVersion))
}