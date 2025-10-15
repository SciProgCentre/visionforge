import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import space.kscience.gradle.useApache2Licence
import space.kscience.gradle.useSPCTeam

plugins {
    id("space.kscience.gradle.project")
    alias(spclibs.plugins.kotlinx.kover)
}

val dataforgeVersion by extra("0.10.1")

allprojects {
    group = "space.kscience"
    version = "0.6.0-dev-1"
}

subprojects {
    if (name.startsWith("visionforge")) apply<MavenPublishPlugin>()

//    repositories {
//        mavenLocal()
//        maven("https://repo.kotlin.link")
//        mavenCentral()
//        maven("https://maven.jzy3d.org/releases")
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
//    }

    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension>{
            compilerOptions{
                freeCompilerArgs.addAll("-Xcontext-parameters")
            }
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
    repository("spc", "https://maven.sciprog.center/kscience")
    central()
}

apiValidation {
    ignoredPackages.add("info.laht.threekt")
}

readme.readmeTemplate = file("docs/templates/README-TEMPLATE.md")