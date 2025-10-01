plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.a1150070050_nguyenngoctuvy_qlpk_dagk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.a1150070050_nguyenngoctuvy_qlpk_dagk"
        minSdk = 23
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
}

dependencies {
    // Android mặc định
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Google Sign-In
    implementation("androidx.security:security-crypto:1.0.0")
    // Test mặc định
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    // phòng khi dùng CardView
    // ✅ Thư viện để gọi API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ✅ Thư viện parse JSON (tuỳ chọn)
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ JUnit cho test Java console
    testImplementation("junit:junit:4.13.2")
}
