package com.ancientlightstudios.simplegen.maven

import org.apache.maven.plugin.logging.Log
import java.util.*

object Workarounds {

    /**
     * For some odd reason, the ResourceBundle is not found when running the plugin from maven. If I load it
     * early, it works. This is a workaround for that.
     */
    fun loadResourceBundle(log: Log) {
        try {
             ResourceBundle.getBundle("jinjava.de.odysseus.el.misc.LocalStrings")
        } catch (e: Exception) {
            log.error(e)
        }
    }
}