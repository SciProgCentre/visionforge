plugins {
    id("scientifik.js")
}

kotlin {
    target {
        useCommonJs()
    }
}


dependencies{

    api("org.jetbrains:kotlin-react:16.13.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:16.13.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains.kotlinx:kotlinx-html:0.6.12")

    api("org.jetbrains:kotlin-extensions:1.0.1-pre.104-kotlin-1.3.72")
    api("org.jetbrains:kotlin-css-js:1.0.0-pre.94-kotlin-1.3.70")
    api("org.jetbrains:kotlin-styled:1.0.0-pre.104-kotlin-1.3.72")

    api(npm("core-js", "2.6.5"))

    api(npm("react", "16.13.1"))
    api(npm("react-dom", "16.13.1"))

    api(npm("react-is", "16.13.0"))
    api(npm("inline-style-prefixer", "5.1.0"))
    api(npm("styled-components", "4.3.2"))
}