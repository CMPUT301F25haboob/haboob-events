plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.haboob"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.haboob"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Maps SDK for Android
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    // Testing
    // For FragmentScenario
    debugImplementation("androidx.fragment:fragment-testing:1.7.1")
    // For TestNavHostController (using the same version as your other navigation libraries)
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.4")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.navigation.testing)
    implementation(libs.fragment.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // dependencies for using URLS to display images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ZXing for QR code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // For storing images in Firebase storage (cloud storage)
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // JUnit 4 for local unit tests
    //testImplementation("junit:junit:4.13.2")
    //androidTestImplementation("androidx.test.ext:junit:1.1.5")
    //androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // JavaDocs
    // implementation(files("/home/user/Android/Sdk/platforms/android-36/android.jar"))


    // mockito testing dependencies - david
    testImplementation("org.mockito:mockito-core:5.+")         // unit tests (JVM)
    androidTestImplementation("org.mockito:mockito-android:5.+")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("com.google.android.gms:play-services-tasks:18.0.2")

    // flexbox for event filters:
    // implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Cloudinary dependencies for storing images
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
}