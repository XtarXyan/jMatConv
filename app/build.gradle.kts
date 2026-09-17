plugins {
    application
    java
    eclipse
    // id("com.gradleup.shadow") version "9.6.1"
    id("org.graalvm.buildtools.native") version "1.1.11"
}

apply(plugin = "java")
// apply(plugin = "com.gradleup.shadow")

repositories {
    mavenCentral()
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  dependencies {
    // classpath("com.gradleup.shadow:shadow-gradle-plugin:<version>")
  }
}

dependencies {
    implementation(libs.guava)

    implementation("info.picocli:picocli:4.7.7")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
    implementation("com.alexdupre:pngj:2.1.2.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter("6.0.1")
            
            targets.all {
                testTask.configure {
                    testLogging {
                        events("passed", "skipped", "failed")
                        showStandardStreams = true
                    }
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.GRAAL_VM
    }
    withJavadocJar()
    withSourcesJar()
}

application {
    // Define the main class for the application.
    mainClass = "com.xtarxyan.JMatConv"
}

graalvmNative {
    toolchainDetection.set(true)

    binaries {
        named("main") {
            imageName.set("jMatConv")
            mainClass.set("com.xtarxyan.JMatConv")
            buildArgs.add("-march=native")
            buildArgs.add("-O3")
            buildArgs.add("--gc=G1")
        }
    }
}

