plugins {
    id("maven-publish")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

group = "com.github.Alchemy-Pay"
version = "1.0.2"

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



