plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("kotlin-android-extensions")
}

androidExtensions {
    features = setOf("parcelize")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.github.brewin.mvicoroutines"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //multiDexEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    sourceSets {
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        //freeCompilerArgs = listOf("-Xnew-inference")
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:_")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:_")
    implementation("androidx.core:core-ktx:_")

    // UI
    implementation("androidx.appcompat:appcompat:_")
    implementation("androidx.activity:activity-ktx:_")
    implementation("androidx.fragment:fragment-ktx:_")
    implementation("androidx.constraintlayout:constraintlayout:_")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:_")
    implementation("com.google.android.material:material:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-appcompat:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-swiperefreshlayout:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:_")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-material:_")

    // Data
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:_")
    implementation("com.squareup.okhttp3:okhttp:_")
    implementation("io.ktor:ktor-client-okhttp:_")
    implementation("io.ktor:ktor-client-json-jvm:_")
    implementation("io.ktor:ktor-client-serialization-jvm:_")
    implementation("io.ktor:ktor-client-logging-jvm:_")

    // Logging
    implementation("com.jakewharton.timber:timber:_")

    // Testing
    testImplementation("junit:junit:_")
    androidTestImplementation("androidx.test:runner:_")
    androidTestImplementation("androidx.test.espresso:espresso-core:_")
}