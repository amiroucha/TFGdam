plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.22"
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tfg_1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tfg_1"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    //-------------material 3--------------
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")


    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.media3:media3-common-ktx:1.7.1")
    implementation("androidx.room:room-common-jvm:2.7.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Para LiveData y ViewModel en Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // --------------------------------------------
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.compose.material:material-icons-extended")

    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-firestore")

    //-----------------------------------------------
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("androidx.credentials:credentials:1.5.0-rc01")
    implementation ("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")



    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    //grafico
    implementation ("com.github.tehras:charts:0.2.4-alpha")


    //retrofit y api
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    //coil para asyncImage
    implementation ("io.coil-kt:coil-compose:2.5.0")

}