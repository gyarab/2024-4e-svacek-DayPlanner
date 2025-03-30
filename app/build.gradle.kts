plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    /** Firebase */
}

android {
    namespace = "com.example.dayplanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dayplanner"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    implementation(libs.navigation.fragment)
    //implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    /** Firebase */
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    /** Dependencies for Firebase products */
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    /**Auth */
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-core:21.1.1")

    implementation("com.firebaseui:firebase-ui-auth:7.2.0")

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.facebook.android:facebook-android-sdk:latest.release")

    /** design **/
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}