import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    android("application")
    kotlin("android")
    kotlin("kapt")
    androidx("navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(sdkVersion)
    buildToolsVersion(buildToolVersion)

    defaultConfig {
        minSdkVersion(sdkMinVersion)
        targetSdkVersion(sdkVersion)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        isEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        // We have to add the explicit cast before accessing the options itself.
        // If we don't, it does not work: "unresolved reference: jvmTarget"
        @Suppress("USELESS_CAST") val options = this as KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
    implementation("androidx.appcompat:appcompat:$androidxAppCompatVersion")
    implementation("androidx.core:core-ktx:$androidxCoreVersion")
    implementation("androidx.fragment:fragment-ktx:$androidxFragmentVersion")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintLayoutVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycle")
    implementation("androidx.dynamicanimation:dynamicanimation:$androidxDynamicAnimationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$androidxNavigationVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$androidxNavigationVersion")
    implementation("com.google.firebase:firebase-auth:$firebaseAuthenticationVersion")
    implementation("com.firebaseui:firebase-ui-auth:$firebaseAuthenticationUiVersion")
    implementation("com.google.firebase:firebase-analytics:$firebaseAnalyticsVersion")
    implementation("com.google.firebase:firebase-firestore-ktx:$firebaseFirestoreVersion")
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVersion")
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$androidxTestJunitVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$androidxTestEspressoVersion")
}
