import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp") version "2.3.6"
}

android {
    signingConfigs {

        val keystoreProperties = Properties().apply {
            val keystoreFile = rootProject.file("keystore.properties")
            if (keystoreFile.exists()) {
                load(keystoreFile.inputStream())
            } else {
                throw GradleException("keystore.properties nicht gefunden!")
            }
        }

        getByName("debug") {
            storeFile = rootProject.file(keystoreProperties["DEBUG_KEY_STORE_FILE"] as String)
            storePassword = keystoreProperties["DEBUG_KEYSTORE_PASSWORD"] as String
            keyAlias = keystoreProperties["DEBUG_KEY_ALIAS"] as String
            keyPassword = keystoreProperties["DEBUG_KEY_PASSWORD"] as String
        }
        create("release") {
            storeFile = rootProject.file(keystoreProperties["RELEASE_KEY_STORE_FILE"] as String)
            storePassword = keystoreProperties["RELEASE_KEYSTORE_PASSWORD"] as String
            keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"] as String
            keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"] as String
        }
    }
    namespace = "com.rettungshundeEinsatzApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.RettungshundeEinsatzApp"
        minSdk = 29
        targetSdk = 36
        versionCode = 66
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("Boolean", "IS_DEBUG_BUILD", "true")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "IS_DEBUG_BUILD", "false")

        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/INDEX.LIST",
                "/META-INF/DEPENDENCIES"
            )
        }
    }

    sourceSets {
        getByName("debug") {
            java.directories.addAll(
                listOf(
                    "build/generated/ksp/debug/kotlin",
                    "build/generated/ksp/debug/java"
                )
            )
        }
        getByName("release") {
            java.directories.addAll(
                listOf(
                    "build/generated/ksp/release/kotlin",
                    "build/generated/ksp/release/java"
                )
            )
        }
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

composeCompiler {
    includeComposeMappingFile.set(false)
}

// deiner App das Schema-Verzeichnis über KSP mitgeben:
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

configurations.all {
    exclude(group = "org.xmlpull", module = "xmlpull")
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.location)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.okhttp)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.osmdroid.android)
    implementation(libs.colorpicker.compose)
    implementation(platform(libs.androidx.compose.bom))
}