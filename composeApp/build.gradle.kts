import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.ComposeHotRun

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.hot.reload)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.compose.unstyled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            kotlin("reflect")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs){
                    exclude("org.jetbrains.compose.material")
            }
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("sh.bitsy.app.kutility.AppKt")
}

compose.desktop {
    application {
        mainClass = "sh.bitsy.app.kutility.AppKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "sh.bitsy.app.kutility"
            packageVersion = "1.0.0"
        }
    }
}
