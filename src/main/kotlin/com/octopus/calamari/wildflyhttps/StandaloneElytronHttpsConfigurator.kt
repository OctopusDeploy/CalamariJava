package com.octopus.calamari.wildflyhttps

import com.octopus.calamari.utils.impl.WildflyService

private const val KEYSTORE_NAME = "octopusHttpsKS"
private const val KEYMANAGER_NAME = "octopusHttpsKM"
private const val SERVER_SECURITY_CONTEXT_NAME = "octopusHttpsSSC"

/**
 * A service for configuring HTTPS in a domain that supports Elytron
 */
class StandaloneElytronHttpsConfigurator : WildflyHttpsConfigurator {


    override fun configureHttps(options: WildflyHttpsOptions, service: WildflyService) {
        /*
            Start by taking a snapshot of the config
         */
        service.apply {
            takeSnapshot()
        }.apply {
            /*
                Start by ensuring the keystore exists
             */
            this.runCommand(
                    "/subsystem=elytron/key-store=${KEYSTORE_NAME}:read-resource",
                    "Reading existing keystore")
                    .onSuccess {
                        if (!it.isSuccess) {
                            /*
                                Create the keystore
                             */
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-store=${KEYSTORE_NAME}:add(" +
                                            "path=${options.createKeystore()},credential-reference={clear-text=${options.fixedPrivateKeyPassword}},type=JKS)",
                                    "Adding the Elytron key store",
                                    "WILDFLY-HTTPS-ERROR-0009",
                                    "There was an error adding the Elytron key store.")
                        } else {
                            /*
                                Configure the keystore
                             */
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-store=${KEYSTORE_NAME}:write-attribute(name=path, value=${options.createKeystore()})",
                                    "Configuring the Elytron key store path",
                                    "WILDFLY-HTTPS-ERROR-0010",
                                    "There was an error configuring the Elytron keystore path.")
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-store=${KEYSTORE_NAME}:write-attribute(name=credential-reference, value={clear-text=${options.fixedPrivateKeyPassword}})",
                                    "Configuring the Elytron key store credentials",
                                    "WILDFLY-HTTPS-ERROR-0010",
                                    "There was an error configuring the Elytron keystore credentials.")
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-store=${KEYSTORE_NAME}:write-attribute(name=type, value=JKS)",
                                    "Configuring the Elytron key store type",
                                    "WILDFLY-HTTPS-ERROR-0010",
                                    "There was an error configuring the Elytron keystore type.")
                        }
                    }
        }.apply {
            /*
               Ensure the key manager exists
            */
            this.runCommand(
                    "/subsystem=elytron/key-manager=${KEYMANAGER_NAME}:read-resource",
                    "Reading existing keymanager")
                    .onSuccess {
                        if (!it.isSuccess) {
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-manager=${KEYMANAGER_NAME}:add(" +
                                            "key-store=${KEYSTORE_NAME},credential-reference={clear-text=${options.fixedPrivateKeyPassword}})",
                                    "Adding the Elytron key manager",
                                    "WILDFLY-HTTPS-ERROR-0011",
                                    "There was an error adding the Elytron key manager.")
                        } else {
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-manager=${KEYMANAGER_NAME}:write-attribute(name=key-store, value=${KEYSTORE_NAME})",
                                    "Configuring the Elytron key manager key store",
                                    "WILDFLY-HTTPS-ERROR-0012",
                                    "There was an error configuring the Elytron key manager key store.")
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/key-manager=${KEYMANAGER_NAME}:write-attribute(name=credential-reference, value={clear-text=${options.fixedPrivateKeyPassword}})",
                                    "Configuring the Elytron key manager credential reference",
                                    "WILDFLY-HTTPS-ERROR-0012",
                                    "There was an error configuring the Elytron key manager credential reference.")
                        }
                    }
        }.apply {
            /*
              Ensure the server ssl context exists
           */
            this.runCommand(
                    "/subsystem=elytron/server-ssl-context=${SERVER_SECURITY_CONTEXT_NAME}:read-resource",
                    "Reading existing server ssl context")
                    .onSuccess {
                        if (!it.isSuccess) {
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/server-ssl-context=${SERVER_SECURITY_CONTEXT_NAME}:add(" +
                                            "key-manager=${KEYMANAGER_NAME},protocols=[\"TLSv1.2\"])",
                                    "Adding the Elytron server ssl context",
                                    "WILDFLY-HTTPS-ERROR-0013",
                                    "There was an error adding the Elytron server ssl context.")
                        } else {
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/server-ssl-context=${SERVER_SECURITY_CONTEXT_NAME}:write-attribute(name=key-manager, value=${KEYMANAGER_NAME})",
                                    "Configuring the Elytron server ssl context key manager",
                                    "WILDFLY-HTTPS-ERROR-0014",
                                    "There was an error configuring the Elytron server ssl context key manager.")
                            this.runCommandExpectSuccess(
                                    "/subsystem=elytron/server-ssl-context=${SERVER_SECURITY_CONTEXT_NAME}:write-attribute(name=protocols, value=[\"TLSv1.2\"])",
                                    "Configuring the Elytron server ssl context protocols",
                                    "WILDFLY-HTTPS-ERROR-0014",
                                    "There was an error configuring the Elytron server ssl context protocols.")
                        }
                    }
        }.apply {
            /*
            this.enterBatchMode()
            this.runCommandExpectSuccess(
                    "/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)",
                    "Removing the legacy security realm",
                    "WILDFLY-HTTPS-ERROR-0005",
                    "There was an error removing the legacy security realm."
            )
            this.runCommandExpectSuccess(
                    "/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context,value=${SERVER_SECURITY_CONTEXT_NAME})",
                    "Adding the Elytron security context",
                    "WILDFLY-HTTPS-ERROR-0006",
                    "There was an error adding the Elytron security context."
            )
            this.runBatch(
                    "WILDFLY-HTTPS-ERROR-0007",
                    "There was an error with the batched operation to remove the legacy security realm and add the Elytron security context.")
                    */
        }.apply {
            this.runCommandExpectSuccess(
                    "reload",
                    "Reloading the server",
                    "WILDFLY-HTTPS-ERROR-0008",
                    "There was an error reloading the server."
            )
        }
    }
}