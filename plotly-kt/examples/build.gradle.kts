plugins {
    kotlin("jvm")
}

repositories {
    maven("https://repo.kotlin.link")
    mavenCentral()
}

dependencies {
    implementation(projects.plotlyKt.plotlyKtServer)
//    implementation(projects.plotly.plotlyktScript)
    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:dataframe:0.15.0")
}

kotlin{
    jvmToolchain(17)
}

// A workaround for https://youtrack.jetbrains.com/issue/KT-44101

val copyPlotlyResources by tasks.registering(Copy::class){
    dependsOn(":plotly-kt:plotly-kt-server:jvmProcessResources")
    mustRunAfter(":plotly-kt:plotly-kt-server:jvmTestProcessResources")
    from(project(":plotly-kt:plotly-kt-server").layout.buildDirectory.file("processedResources/jvm/main"))
    into(layout.buildDirectory.file("resources/main"))
}

tasks.getByName("classes").dependsOn(copyPlotlyResources)

//val runDynamicServer by tasks.creating(JavaExec::class){
//    group = "run"
//    classpath = sourceSets["main"].runtimeClasspath
//    main = "DynamicServerKt"
//}
//
//val runCustomPage by tasks.creating(JavaExec::class){
//    group = "run"
//    classpath = sourceSets["main"].runtimeClasspath
//    main = "CustomPageKt"
//}