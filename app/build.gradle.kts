import com.android.build.api.variant.HostTestBuilder
import java.util.Properties

val signingProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
}
fun signingProperty(name: String): String? = signingProperties.getProperty(name)
    ?: System.getenv("DOPAMINE_${name.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()}")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.dopamine"
    compileSdk = 37

    signingConfigs {
        val storeFile = signingProperty("releaseStoreFile")
        val storePassword = signingProperty("releaseStorePassword")
        val keyAlias = signingProperty("releaseKeyAlias")
        val keyPassword = signingProperty("releaseKeyPassword")
        if (listOf(storeFile, storePassword, keyAlias, keyPassword).all { !it.isNullOrBlank() }) {
            create("release") {
                this.storeFile = file(storeFile!!)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "app.dopamine"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "0.3.2"
        buildConfigField(
            "String", "CONTENT_BASE_URL",
            "\"https://raw.githubusercontent.com/Kmassidik/dopamine-content/main/\""
        )
        buildConfigField(
            "String", "APP_RELEASES_URL",
            "\"https://api.github.com/repos/Kmassidik/dopamine/releases/latest\""
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
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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
    implementation(project(":shared"))
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
