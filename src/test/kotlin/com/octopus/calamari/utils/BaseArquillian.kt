package com.octopus.calamari.utils

import com.octopus.calamari.utils.impl.XMLUtilsImpl
import org.apache.commons.collections4.iterators.NodeListIterator
import org.funktionale.tries.Try
import org.jboss.arquillian.junit.Arquillian
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

const val HTTPS_PORT = 38443

open class BaseArquillian(testClass: Class<*>?) : Arquillian(testClass) {
    /**
    Save some values unrelated to the certificate. The test will ensure these values are preserved.
     */
    fun addConnectorAttributes(xmlFile: String) =
            File(xmlFile).run {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)
            }.apply {
                XMLUtilsImpl.xpathQueryNodelist(
                        this,
                        "//Connector[@port='$HTTPS_PORT']").run {
                    NodeListIterator(this)
                }.forEach {
                    it.attributes.setNamedItem(it.ownerDocument.createAttribute(MAX_HTTP_HEADER_SIZE)
                            .apply { nodeValue = MAX_HTTP_HEADER_SIZE_VALUE })
                    it.attributes.setNamedItem(it.ownerDocument.createAttribute(MAX_THREADS)
                            .apply { nodeValue = MAX_THREADS_VALUE })
                    it.attributes.setNamedItem(it.ownerDocument.createAttribute(MIN_SPARE_THREADS)
                            .apply { nodeValue = MIN_SPARE_THREADS_VALUE })
                }
            }.apply {
                XMLUtilsImpl.saveXML(xmlFile, this)
            }

    /**
     * Deletes the <Connector> element with the matching port
     */
    fun removeConnector(xmlFile: String, port: Int) =
            File(xmlFile).run {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)
            }.apply {
                XMLUtilsImpl.xpathQueryNodelist(
                        this,
                        "//Connector[@port='$port']").run {
                    NodeListIterator(this)
                }.forEach {
                    it.parentNode.removeChild(it)
                }
            }.apply {
                XMLUtilsImpl.saveXML(xmlFile, this)
            }
}