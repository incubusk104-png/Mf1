plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// ---------------------------------------------------------------------------
// Build-time configuration (Supabase + Huawei Account Kit).
//
// Values are resolved from every channel available, in order:
//   1. process environment (private name, then public EXPO_PUBLIC_* name)
//   2. the Rork-managed Config.kt constants in the app source
//   3. committed gradle.properties defaults (see gradle.properties)
// All values are client-public by design (the Supabase anon key is a public
// client key guarded by RLS). Real secrets must NEVER be added to any channel
// here. When a value is blank, the related feature (cloud sync) simply hides.
// ---------------------------------------------------------------------------
val rorkConfigFile = file("src/main/java/com/rork/mindsetframes/Config.kt")

fun rorkConfigValue(key: String): String {
    if (!rorkConfigFile.exists()) return ""
    val text = rorkConfigFile.readText()
    val fromConst = Regex("const val $key = \"([^\"]*)\"").find(text)?.groupValues?.get(1)
    val fromMap = Regex("\"$key\" to \"([^\"]*)\"").find(text)?.groupValues?.get(1)
    return (fromConst ?: fromMap)?.trim().orEmpty()
}

fun gradlePropertyValue(name: String): String =
    providers.gradleProperty(name).orNull?.trim().orEmpty()

fun resolveRorkValue(privateName: String, publicName: String, propertyName: String): String {
    return sequenceOf(System.getenv(privateName), System.getenv(publicName))
        .mapNotNull { it?.trim() }
        .firstOrNull { it.isNotBlank() }
        ?: rorkConfigValue(publicName).ifBlank { gradlePropertyValue(propertyName) }
}

// ---------------------------------------------------------------------------
// Huawei agconnect-services.json support.
// Download the file from AppGallery Connect (Project settings > General
// information > App information) and drop it at android/app/agconnect-services.json
// — the standard Huawei location. The build bundles it into APK assets, where
// the HMS SDK and HuaweiServicesConfig read it at runtime. No agcp Gradle
// plugin is needed, so builds keep working while the file is absent — Huawei
// sign-in simply reports "not configured" until it's added.
// ---------------------------------------------------------------------------
val agconnectGeneratedAssets = layout.buildDirectory.dir("generated/agconnect/assets")
val copyAgconnectServices = tasks.register<Copy>("copyAgconnectServices") {
    from(layout.projectDirectory.file("agconnect-services.json"))
    into(agconnectGeneratedAssets)
}

android {
    namespace = "com.rork.mindsetframestracker"
    compileSdk = 36

    defaultConfig {
        // MUST match the package name registered in AppGallery Connect
        // (agconnect-services.json client.package_name) — HUAWEI ID sign-in
        // and AppGallery upload both reject a mismatch. The Kotlin namespace
        // stays com.rork.mindsetframestracker; only the shipped id differs.
        applicationId = "com.mindsetframes.habittracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "1.0"

        val supabaseUrl = resolveRorkValue("SUPABASE_URL", "EXPO_PUBLIC_SUPABASE_URL", "mindset.supabaseUrl")
        val supabaseAnonKey = resolveRorkValue("SUPABASE_ANON_KEY", "EXPO_PUBLIC_SUPABASE_ANON_KEY", "mindset.supabaseAnonKey")

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")

        println(
            "Rork config — supabase: ${if (supabaseUrl.isBlank()) "missing (sync hidden)" else "resolved"}"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            // agconnect-services.json is staged here by copyAgconnectServices.
            assets.srcDir(agconnectGeneratedAssets)
        }
    }
}

tasks.named("preBuild") {
    dependsOn(copyAgconnectServices)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    constraints {
        // Huawei's hwid → base → stats/device POM chain declares these
        // dependencies WITHOUT versions (a quirk of the HMS repo), which
        // breaks Gradle resolution. Pin the published releases explicitly.
        implementation("com.huawei.hms:network-grs:8.0.1.324")
        implementation("com.huawei.android.hms:security-base:2.0.0.302")
        implementation("com.huawei.android.hms:security-ssl:2.0.0.302")
        implementation("com.huawei.android.hms:security-encrypt:2.0.0.302")
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.koin.androidx.compose)
    // Browser fallback for Supabase OAuth (email sign-in in a Custom Tab).
    implementation(libs.androidx.browser)
    // Huawei Account Kit — native HUAWEI ID sign-in (see auth/HuaweiAuthClient.kt).
    implementation(libs.huawei.hwid)
    // AGConnect core — reads agconnect-services.json so Account Kit initializes
    // (see auth/HuaweiServicesConfig.kt). No agcp Gradle plugin required.
    implementation(libs.huawei.agconnect.core)
    // WorkManager — automated daily cloud backup (see data/CloudBackupWorker.kt).
    implementation(libs.androidx.work.runtime.ktx)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.androidx.ui.tooling)
}
