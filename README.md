# __detekt-compiler-plugin__

[![Download](https://api.bintray.com/packages/arturbosch/code-analysis/detekt-compiler-plugin/images/download.svg) ](https://bintray.com/arturbosch/code-analysis/detekt-compiler-plugin/_latestVersion)

WIP: Experimental support for integrating detekt as a Kotlin compiler plugin

![image](docs/detekt-compiler-plugin.png "image")


### Usage

```kotlin
buildscript {
    repositories {
        maven { setUrl("https://dl.bintray.com/arturbosch/code-analysis") }
        mavenLocal()
    }
    dependencies {
        classpath("io.github.detekt:detekt-compiler-plugin:0.1.2")
    }
}

plugins {
    kotlin("jvm")
}

apply(plugin = "detekt-compiler-plugin")
```

### Limitations

- no custom configs / just default config
- no baseline or other detekt flags yet
- no full path shown
