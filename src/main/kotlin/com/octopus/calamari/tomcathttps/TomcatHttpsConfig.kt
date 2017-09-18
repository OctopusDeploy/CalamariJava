package com.octopus.calamari.tomcathttps

import com.octopus.calamari.utils.Constants
import com.octopus.calamari.utils.impl.ErrorMessageBuilderImpl
import com.octopus.calamari.utils.impl.LoggingServiceImpl
import com.octopus.calamari.utils.impl.XMLUtilsImpl
import org.apache.commons.collections4.iterators.NodeListIterator
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.FileWriter
import java.util.logging.Level
import java.util.logging.Logger
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


object TomcatHttpsConfig {

    val logger: Logger = Logger.getLogger("")

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            LoggingServiceImpl.configureLogging()
            configureHttps(TomcatHttpsOptions.fromEnvironmentVars())
        } catch (ex: Exception){
            logger.log(Level.SEVERE, ErrorMessageBuilderImpl.buildErrorMessage(
                    "TOMCAT-HTTPS-ERROR-0001",
                    "An exception was thrown during the HTTPS configuration."),
                    ex)
            System.exit(Constants.FAILED_HTTPS_CONFIG_RETURN)
        }

        System.exit(0)
    }

    fun configureHttps(options:TomcatHttpsOptions) {
        options
                .apply{ validate() }
                .run { processXml(this) }
                .apply {
                    XMLUtilsImpl.saveXML(
                            "${options.tomcatLocation}${File.separator}conf${File.separator}server.xml",
                            this)
                }
    }

    /**
     * Loads the XML file and processes the matching service node
     */
    private fun processXml(options:TomcatHttpsOptions): Document =
        File(options.tomcatLocation, "conf${File.separator}server.xml")
                .run { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this) }
                .apply {
                    XMLUtilsImpl.createOrReturnElement(
                                this.documentElement,
                                "Service",
                                mapOf(Pair("name", options.service)),
                                mapOf(),
                                false)
                        .map {
                            XMLUtilsImpl.createOrReturnElement(
                                    it, "Connector",
                                    mapOf(Pair("port", options.port.toString()))).get()
                        }
                        .forEach { options.getConfigurator().processConnector(options, it) }
                }

}