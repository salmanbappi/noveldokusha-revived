package my.noveldoksuha.convention.plugin

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroid(
    commonExtension: CommonExtension<
        out BuildFeatures,
        out BuildType,
        out DefaultConfig,
        out ProductFlavor,
        out AndroidResources,
        *> // Installation or whatever the last one is, we don't access it so * is fine?
           // Wait, if I use * for the last one, it might be okay.
) {
    commonExtension.apply {
        compileSdk = appConfig.COMPILE_SDK

        defaultConfig {
            minSdk = appConfig.MIN_SDK

            testInstrumentationRunnerArguments["clearPackageData"] = "true"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildFeatures {
            buildConfig = true
        }

        compileOptions {
            sourceCompatibility = appConfig.javaVersion
            targetCompatibility = appConfig.javaVersion
        }

        lint {
            showAll = true
            abortOnError = false
            lintConfig = rootProject.file("lint.xml")
        }

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-Xjvm-default=all-compatibility")
        }
    }
}
