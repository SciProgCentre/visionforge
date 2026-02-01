import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import space.kscience.gradle.useApache2Licence
import space.kscience.gradle.useSPCTeam

plugins {
    id("space.kscience.gradle.project")
    alias(spclibs.plugins.kotlinx.kover)
}

val dataforgeVersion by extra("0.10.2")

allprojects {
    group = "space.kscience"
    version = "0.5.2-dev-1"
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

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xcontext-parameters")
        }
    }

//    tasks.withType<KotlinJsCompile>{
//        kotlinOptions{
//            useEsClasses = true
//        }
//    }
}


kscienceProject {
    pom("https://github.com/SciProgCentre/visionforge") {
        useApache2Licence()
        useSPCTeam()
    }
    publishTo("spc", "https://maven.sciprog.center/kscience")
    publishToCentral()

    abiValidation {
//        filters{
//            excluded{
//                byNames
//            }
//        }
        //ignoredPackages.add("info.laht.threekt")
    }

    readme.readmeTemplate = file("docs/templates/README-TEMPLATE.md")
}


