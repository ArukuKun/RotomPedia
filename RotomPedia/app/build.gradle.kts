// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // KSP y Compose Compiler se definieron en la raíz, aquí solo se aplican.
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.duoc.rotompedia"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.duoc.rotompedia"
        minSdk = 25
        targetSdk = 36
        versionCode = 30300
        versionName = "3.3.0-rc1"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.core:core-ktx:1.13.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")

    // Generador de código Moshi (KSP)
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Navegación y Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // --- TESTING UI (Instrumentado - Carpeta androidTest) ---
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Herramientas Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- TESTING UNITARIO (Carpeta test) ---
    testImplementation("junit:junit:4.13.2")
    // Mockito 5 incluye mocks inline y soporta el JDK 21 del entorno local.
    testImplementation("org.mockito:mockito-core:5.14.2")

    // Corrutinas y Arquitectur a
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

}
