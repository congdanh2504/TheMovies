plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.home"

}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coil.compose)
}
