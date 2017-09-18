package com.octopus.calamari.tomcat85

import com.octopus.calamari.tomcat7.Tomcat7ArquillianAPR
import com.octopus.calamari.tomcathttps.TomcatHttpsConfig
import com.octopus.calamari.tomcathttps.TomcatHttpsImplementation
import com.octopus.calamari.tomcathttps.TomcatHttpsOptions
import com.octopus.calamari.utils.BaseArquillian
import org.jboss.arquillian.junit.Arquillian
import java.io.File

/**
 * A custom implementation of the Arquillian BlockJUnit4ClassRunner which
 * configures the server.xml file before Tomcat is booted.
 */
class Tomcat85ArquillianAPRMultiple(testClass: Class<*>?) : BaseArquillian(testClass) {
    init {
        TomcatHttpsConfig.configureHttps(TomcatHttpsOptions(
                TOMCAT_VERSION_INFO,
                "target" + File.separator + "config" + File.separator + TOMCAT_VERSION,
                "Catalina",
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.key").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.crt").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.keystore").file).absolutePath,
                "changeit",
                38443,
                TomcatHttpsImplementation.APR,
                "firsthost",
                true))

        addConnectorAttributes(SERVER_XML)

        TomcatHttpsConfig.configureHttps(TomcatHttpsOptions(
                TOMCAT_VERSION_INFO,
                "target" + File.separator + "config" + File.separator + TOMCAT_VERSION,
                "Catalina",
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.key").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.crt").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.keystore").file).absolutePath,
                "changeit",
                38443,
                TomcatHttpsImplementation.APR,
                "secondhost",
                false))

        TomcatHttpsConfig.configureHttps(TomcatHttpsOptions(
                TOMCAT_VERSION_INFO,
                "target" + File.separator + "config" + File.separator + TOMCAT_VERSION,
                "Catalina",
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.key").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.crt").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.keystore").file).absolutePath,
                "changeit",
                38443,
                TomcatHttpsImplementation.APR,
                "_default_",
                false))

        TomcatHttpsConfig.configureHttps(TomcatHttpsOptions(
                TOMCAT_VERSION_INFO,
                "target" + File.separator + "config" + File.separator + TOMCAT_VERSION,
                "Catalina",
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.key").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.crt").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.keystore").file).absolutePath,
                "changeit",
                38443,
                TomcatHttpsImplementation.APR,
                "",
                false))
    }
}