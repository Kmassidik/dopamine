import com.android.build.api.variant.HostTestBuilder

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.dopamine"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.dopamine"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField(
            "String", "CONTENT_BASE_URL",
            "\"https://raw.githubusercontent.com/Kmassidik/dopamine-content/main/\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        localeFilters += listOf("en")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Personal use: signed with the debug key so `assembleRelease` yields an installable APK.
            // See README for using a real keystore.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/*.version", "DebugProbesKt.bin")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true // the screenshot engine needs compiled resources
        // Design guard: screenshots of the design system are compared with reference images. See TESTING.md.
        screenshotTests.create("screenshotTest") {
            engineVersion = "0.0.1-alpha16"
            targetVariants.add("debug")
            dependencies {
                implementation(libs.compose.ui.tooling)
                implementation("com.android.tools.screenshot:screenshot-validation-api:0.0.1-alpha16")
            }
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

androidComponents {
    // The screenshot engine builds a Compose Preview task per variant and needs unit tests on for each.
    beforeVariants { variant -> variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = true }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
