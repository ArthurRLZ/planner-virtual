import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.20"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        outputModuleName.set("plannerVirtual")
        browser {
            commonWebpackConfig {
                outputFileName = "plannerVirtual.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                // A partir do Kotlin 2.0/2.1, kotlinx.browser e org.w3c.dom saíram da
                // stdlib e viraram uma lib separada. CONFIRAM a versão mais recente em
                // https://github.com/Kotlin/kotlinx-browser antes de buildar — não
                // consegui validar isso compilando localmente.
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            }
        }
    }
}
