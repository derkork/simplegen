buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("com.ancientlightstudios:simplegen-gradle-plugin:1.0.8")
    }
}

apply(plugin = "com.ancientlightstudios.simplegen")