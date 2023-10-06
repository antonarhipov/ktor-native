val ktor_version: String by project
val kotlin_version: String by project

buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-native-image-extension-gradle:0.1.0")
    }
}

plugins {
    application
    kotlin("multiplatform") version "1.9.10"
    id("com.google.cloud.tools.jib") version ("3.4.0")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

kotlin {
    //region original
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    //endregion

    linuxX64 {
        binaries {
            executable(listOf(DEBUG, RELEASE)) {
                entryPoint = "main"
                linkerOpts("--as-needed")
                freeCompilerArgs += "-Xoverride-konan-properties=linkerGccFlags.linux_x64=-lgcc -lgcc_eh -lc"
            }
        }
    }

    macosX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    //region macOS Arm64
    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    //endregion

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.arrow-kt:suspendapp:0.4.0")
                implementation("io.arrow-kt:suspendapp-ktor:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-server-test-host:$ktor_version")
            }
        }
    }
}

tasks.register<Copy>("copyBinary") {
    dependsOn(tasks.first { it.name.contains("linkReleaseExecutable") })
    from(layout.buildDirectory.file("bin/linuxX64/releaseExecutable/ktor-native.kexe"))
//    into(layout.buildDirectory.dir("native/nativeCompile"))
    into(layout.buildDirectory.dir("app"))
}

tasks.withType<com.google.cloud.tools.jib.gradle.JibTask> {
    dependsOn("copyBinary")
}

jib {
    from {
        image = "gcr.io/distroless/base"
//        image = "alpine"
    }
    pluginExtensions {
        pluginExtension {
            implementation = "com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension"
            properties = mapOf(Pair("imageName", "ktor-native.kexe"))
        }
    }
    container {
        mainClass = "ApplicationKt"
    }
}

//sourceSets.create("main")