plugins {
    alias(libs.plugins.noveldokusha.android.library)
    alias(libs.plugins.noveldokusha.android.compose)
}

android {
    namespace = "my.noveldokusha.text_to_speech"
}

dependencies {
    implementation(projects.core)
    implementation(projects.tooling.algorithms)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.gemini)
}