plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.room)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.room:room-testing:2.7.1")
}
