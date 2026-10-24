plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.nmims.songstudio"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.nmims.songstudio"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-demo"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui-android:1.10.4")
    implementation("androidx.compose.foundation:foundation-android:1.10.4")
    implementation("androidx.compose.material3:material3-android:1.4.0")
}
