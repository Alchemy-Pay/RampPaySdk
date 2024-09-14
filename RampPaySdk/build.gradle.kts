plugins {
    id("maven-publish")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.kotlin.native.cocoapods")
}

group = "com.github.Alchemy-Pay"
version = "1.0.1"

kotlin {

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }


    listOf(
        iosArm64(),
        iosX64()
    ).forEach {
        it.binaries.framework {
            baseName = "RampPaySdk"
            isStatic = true
        }
    }

    cocoapods {
        summary = "Kotlin Multiplatform SDK for RampPay"
        homepage = "https://github.com/Alchemy-Pay/RampPay-Sdk"
        version = project.version.toString() // 这里是你想发布的版本号
        ios.deploymentTarget = "14.0"  // 目标 iOS 版本
        framework {
            baseName = "RampPaySdk"
            isStatic = true  // 如果你需要静态框架
        }
    }

    sourceSets {
        commonMain.dependencies {

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            //googlepay
            implementation(libs.google.services.wallet)
            implementation(libs.material)
            implementation(libs.androidx.browser)
            implementation(libs.material)

        }
        iosMain.dependencies {

        }
    }
}

android {
    namespace = "com.ach.ramppaysdk"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}
publishing {
    publications {
        // 发布 Android 版本
        create<MavenPublication>("maven") {
            from(components["kotlin"])

            pom {
                name.set("RampPaySdk")
                description.set("Kotlin Multiplatform SDK for RampPay")
                url.set("https://github.com/Alchemy-Pay/RampPay-Sdk")
            }
        }

    }
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
}


tasks.register("createXCFramework") {
    group = "build"
    dependsOn("linkDebugFrameworkIosArm64", "linkDebugFrameworkIosX64")

    doLast {
        exec {
            commandLine("xcodebuild", "-create-xcframework",
                "-framework", "build/bin/iosArm64/releaseFramework/RampPaySdk.framework",
                "-framework", "build/bin/iosX64/releaseFramework/RampPaySdk.framework",
                "-output", "../RampPaySdk.xcframework")
        }
    }
}



