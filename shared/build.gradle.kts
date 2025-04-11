import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("app.cash.sqldelight")
}

kotlin {
    // Determine host OS.
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget: KotlinNativeTarget = when {
        hostOs == "Mac OS X" -> iosX64("ios") // or iosArm64 based on need
        hostOs == "Linux" -> linuxX64("linux")
        isMingwX64 -> mingwX64("mingw")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    // Uncomment for iOS
    /*
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    */

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                // Datetime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
             dependsOn(commonMain)
             dependencies {
                 // SQLDelight Android Driver
                 implementation("app.cash.sqldelight:android-driver:2.0.1")
                 // Ktor Android client (optional)
                 // api("io.ktor:ktor-client-android:2.3.8")
             }
        }
        val androidUnitTest by getting

        // Uncomment for iOS
        /*
        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                 // SQLDelight Native Driver
                implementation("app.cash.sqldelight:native-driver:2.0.1")
                // Ktor Darwin client (optional)
                // api("io.ktor:ktor-client-darwin:2.3.8")
            }
        }
        val iosTest by getting
        */
    }

    // Uncomment for iOS
    /*
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "shared"
            isStatic = true
        }
        //pod("SQLDelight", "~> 2.0.0") // Example if needed
    }
    */
}

android {
    namespace = "com.ryzoft.bondportfolioapp.shared" // Change to your namespace
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // Needed for SQLDelight
    sourceSets["main"].java.srcDirs("src/androidMain/kotlin", "build/generated/sqldelight/code/BondPortfolioDB/androidMain")

}

sqldelight {
  databases {
    create("BondPortfolioDB") {
      packageName.set("com.ryzoft.bondportfolioapp.db")
    }
  }
}
