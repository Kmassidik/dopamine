// Code shared by the Android app and the future iPhone app: content models, validators, parsers, progress rules.
// Pure Kotlin only (no Android or Java APIs) so it also compiles for iOS.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "app.dopamine.shared"
        compileSdk = 37
        minSdk = 24
        withHostTest { }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
