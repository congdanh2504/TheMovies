plugins {
    alias(libs.plugins.themovies.android.application)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.themovies"

    defaultConfig {
        applicationId = "com.practice.themovies"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:detailmovie"))
    implementation(project(":feature:search"))
    implementation(project(":feature:watchlist"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
}