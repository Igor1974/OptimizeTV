// build.gradle.kts (Project-level)

plugins {
    // Применяем нужные плагины через Kotlin DSL
    val agp_version = "8.10.1"
    id("com.android.application") version agp_version apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}