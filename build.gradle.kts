import space.kscience.gradle.useApache2Licence
import space.kscience.gradle.useSPCTeam

plugins {
    id("space.kscience.gradle.project")
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

val dataforgeVersion by extra("0.8.0")

allprojects {
    group = "space.kscience"
    version = "0.4.1-dev-1"
}

subprojects {
    if (name.startsWith("visionforge")) apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        maven("https://repo.kotlin.link")
        mavenCentral()
        maven("https://maven.jzy3d.org/releases")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

//    tasks.withType<KotlinJsCompile>{
//        kotlinOptions{
//            useEsClasses = true
//        }
//    }
}

ksciencePublish {
    pom("https://github.com/SciProgCentre/visionforge") {
        useApache2Licence()
        useSPCTeam()
    }
    repository("spc","https://maven.sciprog.center/kscience")
    sonatype()
}

apiValidation {
    ignoredPackages.add("info.laht.threekt")
}

readme.readmeTemplate = file("docs/templates/README-TEMPLATE.md")