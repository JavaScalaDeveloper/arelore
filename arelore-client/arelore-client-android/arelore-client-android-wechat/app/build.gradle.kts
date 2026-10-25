plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionName = "1.0.0"
val apkBaseName = "arelore-android-wechat"

android {
    namespace = "com.arelore.android.wechat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arelore.android.wechat"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

// Output e.g. arelore-android-wechat-1.0.0-debug.apk
base {
    archivesName.set("$apkBaseName-$appVersionName")
}

android.applicationVariants.configureEach {
    outputs.configureEach {
        val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        outputImpl.outputFileName = "$apkBaseName-$appVersionName-${buildType.name}.apk"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
}
