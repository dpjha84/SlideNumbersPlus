plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseStoreFile = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { !it.isNullOrBlank() }

// Real AdMob ids for release builds are in gradle.properties.
fun adsValue(name: String): String =
    providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull.orEmpty()

val testAdsAppId = "ca-app-pub-3940256099942544~3347511713"

android {
    namespace = "com.dpjha.slidingnumbersplus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dpjha.slidingnumbersplus"
        minSdk = 23
        targetSdk = 36
        versionCode = 305
        versionName = "3.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // installs next to the Play Store version, so testing never touches a real save
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Google's public test units
            manifestPlaceholders["adsAppId"] = testAdsAppId
            buildConfigField("String", "ADS_BANNER", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADS_REWARDED", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "ADS_INTERSTITIAL", "\"ca-app-pub-3940256099942544/1033173712\"")
        }
        release {
            // With no ids the ad SDK is never started; the placeholder only keeps the manifest valid.
            val appId = adsValue("ADS_APP_ID")
            manifestPlaceholders["adsAppId"] = appId.ifBlank { testAdsAppId }
            buildConfigField("String", "ADS_BANNER", "\"${if (appId.isBlank()) "" else adsValue("ADS_BANNER")}\"")
            buildConfigField("String", "ADS_REWARDED", "\"${if (appId.isBlank()) "" else adsValue("ADS_REWARDED")}\"")
            buildConfigField("String", "ADS_INTERSTITIAL", "\"${if (appId.isBlank()) "" else adsValue("ADS_INTERSTITIAL")}\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)

    // reads progress saved by the 2.x build
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    androidTestImplementation(composeBom)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.2.21")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
