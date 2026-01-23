
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import my.noveldoksuha.convention.plugin.implementation
import my.noveldoksuha.convention.plugin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

class NoveldokushaAndroidComposeBestPracticesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.findByType<ApplicationExtension>()
                ?: extensions.getByType<LibraryExtension>()

            extension.apply {
                buildFeatures {
                    compose = true
                }

                dependencies {
                    val bom = libs.findLibrary("compose-bom").get()
                    add("implementation", platform(bom))
                    
                    implementation(libs.findLibrary("compose-androidx-ui").get())
                    implementation(libs.findLibrary("compose-androidx-ui-tooling").get())
                }

                testOptions {
                    unitTests {
                        // For Robolectric
                        isIncludeAndroidResources = true
                    }
                }
            }

        }
    }
}
