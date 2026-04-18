plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.detailmovie"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
}
