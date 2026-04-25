plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.datastore"
}

dependencies {
    implementation(libs.datastore.preferences)
}
