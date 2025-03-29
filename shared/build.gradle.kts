import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.0.0"
    id("com.google.devtools.ksp")
    id("com.rickclephas.kmp.nativecoroutines")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    val ktorVersion = "3.1.1"

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-android:$ktorVersion")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }
    }
}

android {
    namespace = "com.kromanid.splitzee.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
