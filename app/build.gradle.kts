import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

/**
 * La clave de subida sale primero de Bitwarden Secrets Manager: `con-claves` inyecta
 * SLEEP_NOISE_KEYSTORE_B64 (el `.jks` en base64) y las tres credenciales como variables
 * de entorno, y el keystore se decodifica en el directorio de build, legible solo por
 * el propietario. Si faltan, se usa keystore.properties.
 */
fun signingEnv(key: String): String? =
    System.getenv("SLEEP_NOISE_$key")?.takeIf { it.isNotBlank() }

fun decodeKeystore(base64: String, target: File): File {
    target.parentFile.mkdirs()
    target.delete()
    target.createNewFile()
    runCatching {
        Files.setPosixFilePermissions(target.toPath(), PosixFilePermissions.fromString("rw-------"))
    }
    target.writeBytes(Base64.getDecoder().decode(base64.trim()))
    return target
}

val envStorePassword = signingEnv("STORE_PASSWORD")
val envKeyAlias = signingEnv("KEY_ALIAS")
val envKeyPassword = signingEnv("KEY_PASSWORD")
val envKeystoreFile = signingEnv("KEYSTORE_B64")
    ?.takeIf { envStorePassword != null && envKeyAlias != null && envKeyPassword != null }
    ?.let { decodeKeystore(it, layout.buildDirectory.file("signing/release.jks").get().asFile) }

/**
 * La alternativa local: la clave de subida, si está. El fichero vive fuera del control de versiones, así
 * que un clon recién hecho compila en debug sin más y solo falla al firmar una
 * release, que es cuando de verdad hace falta.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.jjrapps.sleepnoise"
    // compileSdk 37 con targetSdk 36 a proposito: la BOM de Compose 2026.08 y
    // material3 1.5.0-alpha exigen compilar contra la API 37, y compilar contra
    // una API no es optar por su comportamiento en runtime. Ver ADR 005.
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.jjrapps.sleepnoise"
        minSdk = 31
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Sin esto, las traducciones que arrastran las librerias de AndroidX meten
    // cuarenta idiomas en el APK y descuadran el selector de idioma del sistema.
    androidResources {
        localeFilters += listOf("en", "es")
    }

    signingConfigs {
        if (envKeystoreFile != null) {
            create("release") {
                storeFile = envKeystoreFile
                storePassword = envStorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
            }
        } else if (keystoreProperties.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // AGP mete en el APK la lista de dependencias cifrada con una clave de Google:
    // solo Google puede leerla, y F-Droid rechaza los APK que la llevan.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.datastore.preferences)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
