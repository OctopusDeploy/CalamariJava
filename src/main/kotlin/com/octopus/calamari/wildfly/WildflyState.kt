package com.octopus.calamari.wildfly

import com.google.common.base.Splitter
import com.octopus.calamari.utils.impl.LoggingServiceImpl
import org.funktionale.tries.Try
import java.util.logging.Logger

object WildflyState {
    val logger: Logger = Logger.getLogger(WildflyState::class.simpleName)

    @JvmStatic
    fun main(args: Array<String>) {
        WildflyState.setDeploymentState(WildflyOptions.fromEnvironmentVars())

        /*
            org.jboss.as.cli.impl.CLIModelControllerClient has some threads
            that can take a minute to timeout. We really don't want to wait,
            so exit right away.
         */
        System.exit(0)
    }

    init {
        LoggingServiceImpl.configureLogging()
    }

    fun setDeploymentState(options:WildflyOptions) {
        val service = WildflyService().login(options)

        if (service.isDomainMode) {
            /*
                Deploy the package for enabled server groups
             */
            Try.Success(service.takeSnapshot())
                .map {
                    Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(options.enabledServerGroup)
                            .forEach { serverGroup ->
                                service.runCommandExpectSuccess(
                                        "/server-group=$serverGroup/deployment=${options.packageName}:deploy",
                                        "deploy the package ${options.packageName} to the server group $serverGroup",
                                        "WILDFLY-DEPLOY-ERROR-0005: There was an error deploying the " +
                                                "${options.packageName} to the server group $serverGroup"
                                ).onFailure { throw it }
                            }
                }
                /*
                    And undeploy the package for disabled server groups
                 */
                .map {
                    Splitter.on(',')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(options.disabledServerGroup).forEach { serverGroup ->
                        service.runCommandExpectSuccess(
                                "/server-group=$serverGroup/deployment=${options.packageName}:undeploy",
                                "undeploy the package ${options.packageName} from the server group $serverGroup",
                                "WILDFLY-DEPLOY-ERROR-0006: There was an error undeploying the " +
                                        "${options.packageName} to the server group $serverGroup"
                        ).onFailure { throw it }
                    }
                }
        } else {
            Try.Success(service.takeSnapshot())
                .map {
                    service.runCommandExpectSuccess(
                            "${if (options.enabled) "deploy" else "undeploy --keep-content"} --name=${options.packageName}",
                            "enable application in standalone WildFly/EAP instance",
                            "WILDFLY-DEPLOY-ERROR-0012: There was an error enabling or disabling the package ${options.packageName} in the standalone server")
                }
        }
    }
}