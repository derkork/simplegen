package com.ancientlightstudios.simplegen

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

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

}

fun main(args: Array<String>) {
    println("SimpleGen - a simple yet powerful code generator.")
    val jc = JCommander(CommandLineArgs, *args)

    if (CommandLineArgs.version) {
        println(Version.version)
        System.exit(0)
    }

    if (CommandLineArgs.help) {
        jc.usage()
        System.exit(0)
    }

    Runner(CommandLineArgs.sourceDirectory,CommandLineArgs.configFileName, CommandLineArgs.outputDirectory).run()
}