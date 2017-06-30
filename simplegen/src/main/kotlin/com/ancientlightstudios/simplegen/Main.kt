package com.ancientlightstudios.simplegen

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CommandLineArgs {
    @Parameter(names=arrayOf("--sourceDirectory", "-s"), description = "Directory which contains the config.yml file.")
    var sourceDirectory:String = "."
    @Parameter(names=arrayOf("--outputDirectory", "-o"), description = "Directory where the generated files should be placed.")
    var outputDirectory:String = "."
    @Parameter(names= arrayOf("--configFileName", "-c"), description = "Name of the configuration file to use.")
    var configFileName = "config.yml"
    @Parameter(names =arrayOf("--help"), help = true, description = "Shows this help information and exits.")
    var help: Boolean = false
    @Parameter(names =arrayOf("--version"), description = "Shows the version and exits.")
    var version:Boolean = false
    @Parameter(names =arrayOf("--verbose"), description = "Show more verbose output.")
    var verbose:Boolean = false

}

fun main(args: Array<String>) {
    val jc = JCommander(CommandLineArgs, *args)

    if (CommandLineArgs.verbose) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
    }

    val log: Logger = LoggerFactory.getLogger("")
    log.info("SimpleGen - a simple yet powerful code generator.")

    if (CommandLineArgs.version) {
        log.info(Version.version)
        System.exit(0)
    }

    if (CommandLineArgs.help) {
        jc.usage()
        System.exit(0)
    }

    if (!Runner(CommandLineArgs.sourceDirectory,CommandLineArgs.configFileName, CommandLineArgs.outputDirectory).run()) {
        log.info("There were errors when running.")
        System.exit(1)
    }
    else {
        log.info("Generation complete.")
        System.exit(0)
    }
}