import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

subprojects {
    plugins.withType<KotlinPluginWrapper> {
        configure<KotlinProjectExtension> {
            explicitApi = ExplicitApiMode.Disabled
        }
    }
}