buildscript {
    val versions: Map<String, String> = java.util.Properties().apply {
        load(file("versions.properties").reader())
    } as Map<String, String>
    val versionKotlin = "${versions["version.kotlin"]}"

    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0-alpha08")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$versionKotlin")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$versionKotlin")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

configurations.all {
    resolutionStrategy.force("com.squareup.okhttp3:okhttp:_")
}

tasks.withType<Delete> {
    delete(rootProject.buildDir)
}