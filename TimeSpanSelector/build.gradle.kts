import java.util.Properties

/*
 * Copyright 2025 Vivek Umrao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.maven.central.publish)
}

// Load secrets
val secretProps = Properties()
val secretPropsFile = file("H:/Key/secure.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.inputStream().use { secretProps.load(it) }
}

val libGroupID = "io.github.ervivekumrao"
val libArtifactID = "time-span-selector"
val libVersion = "0.0.7-alpha"

android {
    namespace = "vivek.umrao.time.span.selector"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// Configuration for Maven Publish Plugin
// Credentials, Host (S01), and signing are configured via gradle.properties.
mavenPublishing {
    coordinates(libGroupID, libArtifactID, libVersion)

    pom {
        name.set("Time Span Selector")
        description.set("A highly customizable Time Span Selector library for Android (Circular and Linear).")
        url.set("https://github.com/ervivekumrao/TimeSpanSelectorView")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ervivekumrao")
                name.set("Vivek Umrao")
                email.set("manuscriptcode@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ervivekumrao/TimeSpanSelectorView.git")
            developerConnection.set("scm:git:ssh://github.com/ervivekumrao/TimeSpanSelectorView.git")
            url.set("https://github.com/ervivekumrao/TimeSpanSelectorView/tree/main")
        }
    }
}

// Add GitHub Packages as an additional repository.
// The plugin creates a publication named "maven" by default.
publishing {
    repositories {
        if (secretProps.containsKey("GIT_TSS_USER")) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/ervivekumrao/TimeSpanSelectorView")
                credentials {
                    username = secretProps.getProperty("GIT_TSS_USER")
                    password = secretProps.getProperty("GIT_TSS_PASS")
                }
            }
        }
    }
}

// Unified task to publish to both destinations in one command.
tasks.register("publishToAll") {
    description = "Publishes the library to both Maven Central and GitHub Packages."
    group = "publishing"
    dependsOn("publishToMavenCentral")
    dependsOn("publishMavenPublicationToGitHubPackagesRepository")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    // Android Instrumentation Testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
