import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
extra.apply{
    set("MAPBOX_DOWNLOADS_TOKEN", gradleLocalProperties(rootDir).getProperty("MAPBOX_DOWNLOADS_TOKEN"))
}

val hiltVersion by extra("1.0.0-alpha02")

buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build", "gradle", "7.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.google.dagger", "hilt-android-gradle-plugin", "2.40")
    }
}
