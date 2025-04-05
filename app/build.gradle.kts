plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.starstreamvpn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.starstreamvpn"
        minSdk = 26
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.tunnel)
    runtimeOnly(libs.appcompat)
    implementation("com.google.code.gson:gson:2.7")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")

}