plugins {
    alias(libs.plugins.noveldokusha.android.library)
}

android {
    namespace = "my.noveldokusha.text_to_speech"
}

dependencies {
    implementation(projects.tooling.algorithms)
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.gemini)
}
