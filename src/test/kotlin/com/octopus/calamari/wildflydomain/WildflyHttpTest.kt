package com.octopus.calamari.wildflydomain

import com.octopus.calamari.wildflyhttps.WildflyHttpsOptions
import com.octopus.calamari.wildflyhttps.WildflyHttpsStandaloneConfig
import com.octopus.common.WildflyTestBase
import org.apache.commons.io.FileUtils
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(Arquillian::class)
class WildflyHttpTest : WildflyTestBase() {

    @Test
    @RunAsClient
    fun testWildflyCertificateDeployment():Unit =
        WildflyHttpsOptions(
                controller = "127.0.0.1",
                port = System.getProperty("port").toInt(),
                user = System.getProperty("username"),
                password = System.getProperty("password"),
                protocol = System.getProperty("protocol"),
                privateKey = FileUtils.readFileToString(File(this.javaClass.getResource("/octopus.key").file), "UTF-8"),
                publicKey = FileUtils.readFileToString(File(this.javaClass.getResource("/octopus.crt").file), "UTF-8"),
                profiles = "default",
                relativeTo = "jboss.server.config.dir",
                keystoreName = "octopus.keystore"
        ).apply {
            WildflyHttpsStandaloneConfig.configureHttps(this)
        }.run {}
}