// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    //trick: buildscript {} is implicitly applied at the beginning of the file
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.multiplatform") version "1.9.23" apply false
    id("org.jetbrains.compose") version "1.7.3" apply false
    id("app.cash.sqldelight") version "2.0.1" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false // If needed for Compose Destinations etc.
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
