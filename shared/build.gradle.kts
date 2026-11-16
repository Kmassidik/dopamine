// Code shared by the Android app and the future iPhone app: content models, validators, parsers, progress rules.
// Pure Kotlin only (no Android or Java APIs) so it also compiles for iOS.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.ksp)
}

kotlin {
    androidLibrary {
        namespace = "app.dopamine.shared"
        compileSdk = 37
        minSdk = 24
        withHostTest { isReturnDefaultValues = true } // android.util.Log in the shared code returns defaults in JVM tests
        androidResources { enable = true } // native fonts on Android: loaded synchronously, no flash of the system font
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared" // imported by iosApp as `import Shared`
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.components.resources)
            api(libs.kotlinx.coroutines.core)
            api(libs.okio)
            api(libs.jb.navigation.compose)
            api(libs.jb.lifecycle.viewmodel.compose)
            api(libs.jb.lifecycle.runtime.compose)
            api(libs.ktor.client.core)
            api(libs.androidx.room.runtime.kmp)
            api(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.okio.fakefilesystem)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "app.dopamine.shared.resources"
    generateResClass = always
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

ksp {
    // Same place as before the move, so the exported schemas and MigrationSqlTest stay where they were.
    arg("room.schemaLocation", rootProject.file("app/schemas").absolutePath)
}
