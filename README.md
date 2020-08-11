# WIP: __detekt-compiler-plugin__

![Pre Merge Checks](https://github.com/detekt/detekt-compiler-plugin/workflows/Pre%20Merge%20Checks/badge.svg)
[![Download](https://api.bintray.com/packages/arturbosch/code-analysis/detekt-compiler-plugin/images/download.svg) ](https://bintray.com/arturbosch/code-analysis/detekt-compiler-plugin/_latestVersion)

Experimental support for integrating detekt as a Kotlin compiler plugin

![image](docs/detekt-compiler-plugin.png "image")


### Usage

```kotlin
plugins {
    id("io.github.detekt.gradle.compiler-plugin") version "<latest>"
}

detekt {
    isEnabled = true // or with a property: System.getProperty("runDetekt") != null
    // everything from https://detekt.github.io/detekt/kotlindsl.html#options-for-detekt-configuration-closure
    // is supported to declare, only some options are used. See limitations. 
}
```

### Limitations

Everything our Gradle plugin (`DetektExtension`) supports, is also supported on the declaration side with this plugin.  
However only the following options are implemented/passed down to detekt:
- config
- baseline
- debug
- buildUponDefaultConfig
