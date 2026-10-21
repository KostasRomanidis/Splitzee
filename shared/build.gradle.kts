import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            // di
            api(libs.koin.core)
            implementation(libs.sqldelight.coroutines.extensions)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        androidUnitTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlin.test.junit)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

sqldelight {
    databases {
        create("SplitzeeDatabase") {
            packageName.set("com.kromanid.splitzee.database")
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
