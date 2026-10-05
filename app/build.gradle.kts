import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    jacoco
}

// Load signing config from keystore.properties or environment variables
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.rejowan.pdfreaderpro"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.rejowan.pdfreaderpro"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 9
        versionName = "2.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Try keystore.properties first, then environment variables
            storeFile = file(
                keystoreProperties.getProperty("storeFile")
                    ?: System.getenv("KEYSTORE_FILE")
                    ?: "release.keystore"
            )
            storePassword = keystoreProperties.getProperty("storePassword")
                ?: System.getenv("KEYSTORE_PASSWORD")
                ?: ""
            keyAlias = keystoreProperties.getProperty("keyAlias")
                ?: System.getenv("KEY_ALIAS")
                ?: ""
            keyPassword = keystoreProperties.getProperty("keyPassword")
                ?: System.getenv("KEY_PASSWORD")
                ?: ""
        }
    }

    buildTypes {
        debug {
            // Debug builds enable logging
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            // Coverage is measured from the debug unit tests.
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            // Disable logging in release
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            // Use release signing if configured
            if (keystorePropertiesFile.exists() || System.getenv("KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    testOptions {
        // Android framework stubs return defaults instead of throwing, so the PDF
        // rendering paths can be driven from JVM tests with the classes mocked.
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.navigation)

    // Kotlin Serialization (for type-safe navigation)
    implementation(libs.kotlinx.serialization.json)

    // Ktor HTTP Client (for GitHub API)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)


    // Image Loading
    implementation(libs.coil.compose)

    // Logging
    implementation(libs.timber)

    // Animation
    implementation(libs.lottie.compose)

    // Reorderable List (drag-to-reorder)
    implementation(libs.reorderable)

    // Licensy (open source licenses display)
    implementation(libs.licensy.compose)

    // Splash Screen
    implementation(libs.splashscreen)

    // webkit for PDF rendering
    implementation(libs.androidx.webkit)

    // iText PDF processing
    implementation(libs.itext.core) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.itext.bcadapter) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.bouncycastle.provider)
    implementation(libs.bouncycastle.pkix)


    // Testing
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.room.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}

// ============================================================================
// Test coverage
// ============================================================================

jacoco {
    toolVersion = "0.8.12"
}

/**
 * Coverage for the debug unit tests: `./gradlew coverageReport`.
 *
 * Wired by hand rather than through a coverage plugin because the ones available
 * do not yet understand this AGP version's variants, and a report that silently
 * measures nothing is worse than none.
 *
 * The exclusions below are things a unit test cannot speak to, and leaving them in
 * would make the figure a measure of how much UI exists rather than how well the
 * logic is tested:
 *  - generated code
 *  - dependency injection wiring
 *  - the vendored PDF.js bridge, which needs a live WebView and is checked by hand
 *  - Compose UI, which needs an instrumented or Robolectric harness
 */
val coverageExclusions = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/*_Factory*.*", "**/*_Impl*.*", "**/*Companion*.*",
    "**/*\$\$serializer*.*", "**/ComposableSingletons*.*", "**/*ComposableKt*.*",
    "**/di/**",
    "**/appClasses/**",
    "**/presentation/components/pdf/**",
    "**/presentation/components/pdfcompose/**",
    "**/presentation/theme/**",
    "**/presentation/navigation/**",
    // Compose UI. These are top level composable functions, compiled into *Kt
    // classes, and a unit test cannot enter them at all. Counting them would make
    // the figure a measure of how much screen code exists rather than how well the
    // logic behind it is tested.
    "**/presentation/components/**",
    "**/*ScreenKt*.*", "**/*ScreenContentKt*.*",
    "**/*SheetKt*.*", "**/*DialogKt*.*", "**/*BarKt*.*",
    "**/*ItemKt*.*", "**/*CardKt*.*", "**/*OverlayKt*.*",
    "**/*PanelKt*.*", "**/*TabKt*.*", "**/*ViewKt*.*",
    "**/*StateKt*.*", "**/*CaptureKt*.*", "**/*IndicatorKt*.*",
    "**/screens/**/components/**",
    // Declarations that only exist to describe a screen: the tool catalogue, the
    // onboarding pages, the bottom bar entries. They live inside the screen files
    // above but compile to classes of their own, so the patterns there miss them.
    "**/screens/tools/PdfTool*.*", "**/screens/tools/ToolCategory*.*",
    "**/screens/onboarding/OnboardingPage*.*", "**/screens/onboarding/ShapeConfig*.*",
    "**/screens/home/BottomNavItem*.*", "**/screens/home/HomeSubTab*.*",
    // Activities are Compose hosts with no logic of their own, and the crash
    // screen is a composable like any other.
    "**/presentation/MainActivity*.*", "**/presentation/ErrorActivity*.*"
)

tasks.register<JacocoReport>("coverageReport") {
    group = "verification"
    description = "Line and branch coverage for the debug unit tests."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }

    // AGP 9 puts compiled Kotlin under intermediates/built_in_kotlinc rather than
    // the tmp/kotlin-classes path older setups use. Both are listed so this keeps
    // working if that moves again, and Java is included for completeness.
    classDirectories.setFrom(
        files(
            fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
                exclude(coverageExclusions)
            },
            fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(coverageExclusions)
            },
            fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
                exclude(coverageExclusions)
            }
        )
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        }
    )
}
