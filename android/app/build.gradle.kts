import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// ---------------------------------------------------------------------------
// Build-time configuration (Supabase + Huawei Account Kit).
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
// ---------------------------------------------------------------------------
configurations.all {
    exclude(group = "com.huawei.hms", module = "stats")
    exclude(group = "com.huawei.hms", module = "device")
    exclude(group = "com.huawei.hms", module = "network-grs")
}

val agconnectGeneratedAssets = layout.buildDirectory.dir("generated/agconnect/assets")
val copyAgconnectServices = tasks.register<Copy>("copyAgconnectServices") {
    from(layout.projectDirectory.file("agconnect-services.json"))
    into(agconnectGeneratedAssets)
}

android {
    namespace = "com.rork.mindsetframestracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mindsetframes.habittracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 20
        versionName = "1.0.1"

        val supabaseUrl = resolveRorkValue("SUPABASE_URL", "EXPO_PUBLIC_SUPABASE_URL", "mindset.supabaseUrl")
        val supabaseAnonKey = resolveRorkValue("SUPABASE_ANON_KEY", "EXPO_PUBLIC_SUPABASE_ANON_KEY", "mindset.supabaseAnonKey")

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")

        println(
            "Rork config — supabase: ${if (supabaseUrl.isBlank()) "missing (sync hidden)" else "resolved"}"
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
            assets.srcDir(agconnectGeneratedAssets)
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
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
    implementation(libs.androidx.browser)

    // Huawei Account Kit
    implementation(libs.huawei.hwid)

    // Huawei AppGallery Connect Core
    implementation(libs.huawei.agconnect.core)

    // Huawei App Update (JosApps / AppUpdateClient live here)
    implementation(libs.huawei.update)

    // Huawei Health Kit. 6.13.0.302 does not exist on Google Maven, Maven
    // Central, or Huawei's own repo (confirmed against all three — the
    // build failed with "Could not find com.huawei.hms:health:6.13.0.302"
    // on every one of them). Pinned to 6.11.0.300 to match the base/iap
    // release train below, which is the one already confirmed resolvable
    // in this project. If this specific version 404s too, check
    // https://developer.huawei.com/repo/com/huawei/hms/health/maven-metadata.xml
    // for the actual list of versions Huawei has published for this artifact.
    implementation("com.huawei.hms:health:6.11.0.300")

    // HMS Core modules. Note: base/iap do NOT share hwid/appservice's version
    // numbering (6.14.0.300 isn't published for base/iap) — Huawei versions
    // each HMS module independently, so don't assume they move in lockstep.
    // 6.11.0.300 is the latest base/iap release that's actually resolvable.
    implementation("com.huawei.hms:base:6.11.0.300")
    implementation("com.huawei.hms:iap:6.11.0.300")

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.pdfbox.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

tasks.matching {
    it.name.matches(Regex("merge.*Assets")) || it.name.contains("Lint", ignoreCase = true)
}.configureEach {
    dependsOn(copyAgconnectServices)
}
