package com.ancientlightstudios.simplegen.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SimplegenGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
         project.task("hello").doLast {
             println("Hello world!")
         }
    }
}

