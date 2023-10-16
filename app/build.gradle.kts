plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
}

android {
    namespace = "com.androvine.pdfreaderpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.androvine.pdfreaderpro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }



}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // lottie animation
    implementation("com.airbnb.android:lottie:5.2.0")

    // koin for dependency injection
    implementation("io.insert-koin:koin-android:3.5.0")

    // pdf reader
    implementation(project(":android-pdf-viewer"))

    // shared preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // ssp and sdp for responsive ui
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")

}