package com.ancientlightstudios.simplegen.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

abstract class SimplegenBaseMojo : AbstractMojo() {
    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected lateinit var project: MavenProject

    /**
     * The name of the config file.
     */
    @Parameter(defaultValue = "config.yml")
    protected lateinit var configFileName: String

    /**
     * Should an update of the generated sources be forced.
     */
    @Parameter(defaultValue = "false")
    protected var forceUpdate: Boolean = false
}