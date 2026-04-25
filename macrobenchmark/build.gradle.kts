plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.practice.macrobenchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Point at the app module to generate a Baseline Profile for it
    targetProjectPath = ":app"

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.profileinstaller)
}
