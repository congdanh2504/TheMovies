plugins {
    alias(libs.plugins.themovies.android.library)
}

android {
    namespace = "com.practice.data"

}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
}
