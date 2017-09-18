package com.octopus.calamari.tomcat7

import com.octopus.calamari.tomcathttps.TomcatHttpsConfig
import com.octopus.calamari.tomcathttps.TomcatHttpsImplementation
import com.octopus.calamari.tomcathttps.TomcatHttpsOptions
import com.octopus.calamari.utils.*
import com.octopus.calamari.utils.impl.XMLUtilsImpl
import org.jboss.arquillian.junit.Arquillian
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A custom implementation of the Arquillian BlockJUnit4ClassRunner which
 * configures the server.xml file before Tomcat is booted.
 */
class Tomcat7ArquillianAPR(testClass: Class<*>?) : BaseArquillian(testClass) {
    init {
        /*
            Configure with BIO and NIO first to make sure we transform between implementations correctly
        */
        TomcatHttpsConfig.configureHttps(TomcatHttpsOptions(
                TOMCAT_VERSION_INFO,
                "target" + File.separator + "config" + File.separator + TOMCAT_VERSION,
                "Catalina",
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.key").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.crt").file).absolutePath,
                File(Tomcat7ArquillianAPR::class.java.getResource("/octopus.keystore").file).absolutePath,
                "changeit",
                38443,
                TomcatHttpsImplementation.BIO,
                "",
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
                TomcatHttpsImplementation.NIO,
                "",
                false))

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
                "",
                false))
    }
}