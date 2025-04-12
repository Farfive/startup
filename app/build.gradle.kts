import java.util.Properties
import java.io.FileInputStream

1plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jlleitschuh.gradle.ktlint")
}

// Read properties from root gradle.properties file
val gradleProperties = Properties()
val propertiesFile = rootProject.file("gradle.properties")
if (propertiesFile.exists()) {
    gradleProperties.load(FileInputStream(propertiesFile))
}

android {
    namespace = "com.example.styleap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.styleap"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Example: Read API Key from gradle.properties
        // Provides a default value "" if the property is not found
        val dreamApiKey = gradleProperties.getProperty("DREAM_API_KEY") ?: ""
        buildConfigField("String", "DREAM_API_KEY", "\"$dreamApiKey\"")
        
        // Existing buildConfigField for API_BASE_URL (This is fine, but API keys are more common secrets)
        buildConfigField("String", "API_BASE_URL", "\"https://api.yourdomain.com/\"")
        
        // Multidex support if needed
        multiDexEnabled = true
    }
    
    // Add signing config for release builds
    signingConfigs {
        create("release") {
            // These should be stored in local.properties or environment variables
            // or better yet, use the Gradle Secrets Plugin
            storeFile = file("../keystore/release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "password"
            keyAlias = System.getenv("KEY_ALIAS") ?: "key0"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "password"
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
        compose = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            
            // Use a different app ID for debug builds to allow installing both
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // Debug-specific build config fields
            buildConfigField("boolean", "DEBUG_MODE", "true")
            
            // You could override secrets for debug if needed, e.g., using a test API key
            val debugDreamApiKey = gradleProperties.getProperty("DEBUG_DREAM_API_KEY") ?: gradleProperties.getProperty("DREAM_API_KEY") ?: ""
            buildConfigField("String", "DREAM_API_KEY", "\"$debugDreamApiKey\"") // Override if debug key exists
            
            // Override API_BASE_URL for debug
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.yourdomain.com/\"")
            
            // Use debug AdMob app ID
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Use release signing config
            signingConfig = signingConfigs.getByName("release")
            
            // Release-specific build config fields
            buildConfigField("boolean", "DEBUG_MODE", "false")
            
            // Release builds will use the DREAM_API_KEY defined in defaultConfig unless overridden here
            
            // Use production AdMob app ID
            manifestPlaceholders["admobAppId"] = "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
        }
    }
    
    // Configure product flavors if needed (e.g., free vs premium)
    flavorDimensions += "version"
    productFlavors {
        create("free") {
            dimension = "version"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
        }
        
        create("premium") {
            dimension = "version"
            applicationIdSuffix = ".premium"
            versionNameSuffix = "-premium"
        }
    }
    
    // Configure bundle options for app bundle
    bundle {
        language {
            // Specifies that the app bundle should not support
            // configuration APKs for language resources
            enableSplit = true
        }
        density {
            // Enable split for density
            enableSplit = true
        }
        abi {
            // Enable split for ABIs
            enableSplit = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // Configure lint options
    lint {
        // Example: Make all warnings errors
        // warningsAsErrors = true

        // Example: Abort build on error (useful for CI)
        abortOnError = true

        // Example: Disable a specific check
        // disable.add("TypographyFractions")

        // Example: Generate HTML and XML reports
        htmlReport = true
        xmlReport = true
        htmlOutput = file("${project.buildDir}/reports/lint/lint-results.html")
        xmlOutput = file("${project.buildDir}/reports/lint/lint-results.xml")

        // For more options see: https://developer.android.com/reference/tools/gradle-api/7.0/com/android/build/api/dsl/Lint
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.7.6")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    
    // Optional: Hilt integration for common AndroidX libraries (like ViewModel)
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    
    // Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room for local database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // Add Room compiler
    
    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // RecyclerView for lists
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Multidex support if needed
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase SDKs
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    
    // Google AdMob SDK
    implementation(libs.play.services.ads)

    // Google Play Billing
    implementation(libs.billing.ktx)
    
    // Timber for logging
    implementation(libs.timber)

    // --- Jetpack Compose Dependencies ---
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3
    implementation(libs.androidx.compose.material3)

    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Integration with Activities
    implementation(libs.androidx.activity.compose)

    // Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Integration with Navigation
    implementation(libs.androidx.navigation.compose)

    // Optional: Integration with Hilt
    implementation(libs.androidx.hilt.navigation.compose)

    // --- End Jetpack Compose Dependencies ---
}

// Optional: Configure ktlint if needed (defaults are usually good)
// ktlint {
//    version = "1.0.1" // Pin the ktlint engine version
//    verbose = true
//    outputToConsole = true
//    reporters {
//        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
//        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
//    }
// }