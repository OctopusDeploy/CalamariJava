package com.octopus.calamari.wildfly

import com.google.common.base.Preconditions.checkState
import com.octopus.calamari.utils.impl.RetryServiceImpl
import org.funktionale.tries.Try
import org.jboss.`as`.cli.scriptsupport.CLI
import org.springframework.retry.RetryCallback
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger

const val LOGIN_LIMIT = 1000 * 60 * 2L

/**
 * A service used to interact with WildFly
 */
class WildflyService {
    private val logger: Logger = Logger.getLogger(WildflyService::class.simpleName)
    private val retry = RetryServiceImpl.createRetry()
    private val jbossCli = CLI.newInstance()
    /**
     * True once the login() function completes, and false otherwise
     */
    private var connected = AtomicBoolean(false)

    val isDomainMode:Boolean
        /**
         * @return true if the connection was made to a domain controller, and false otherwise
         */
        get() {
            return jbossCli?.commandContext?.isDomainMode ?: false
        }

    fun login(options: WildflyOptions): WildflyService {
        synchronized(jbossCli) {
            /*
                There are cases where the login will stall. If the wildfly-elytron package is not
                properly registered in META-INF/services, there will be a prompt to log in that
                can never be satisfied because there is no input.

                ALthough this should not happen, we have a thread here that can be watched and
                timed out should any inputs like that be requested.
             */
            val thread = Thread(Runnable {
                Try {retry.execute(RetryCallback<Unit, Throwable> { context ->
                    checkState(!connected.get(), "You can not connect more than once")

                    logger.info("Attempt ${context.retryCount + 1} to connect.")

                    jbossCli.connect(
                            options.protocol,
                            options.controller,
                            options.port,
                            options.fixedUsername,
                            options.fixedPassword?.toCharArray())

                    connected.set(true)
                })}
                .onFailure {
                    logger.severe("WILDFLY-DEPLOY-ERROR-0009: There was an error logging into the management API")
                    throw it
                }
            })

            thread.setDaemon(true)
            thread.start()

            /*
                Wait for a while until we are connected
             */
            val startTime = System.currentTimeMillis()
            while (!connected.get() && System.currentTimeMillis() - startTime < LOGIN_LIMIT) {
                Thread.sleep(100)
            }

            /*
                All good? Return this object.
             */
            if (connected.get()) {
                return this
            }

            /*
                We have timed out waiting for a connection
             */
            throw Exception("WILDFLY-DEPLOY-ERROR-0013: The login was not completed in a reasonable amount of time")
        }
    }

    fun logout(): WildflyService {
        synchronized(jbossCli) {
            Try{ retry.execute(RetryCallback<Unit, Throwable> { context ->
                checkState(connected.get(), "You must be connected to disconnect")

                logger.info("Attempt ${context.retryCount + 1} to disconnect.")

                jbossCli.disconnect()

                connected.set(false)
            })}
            .onFailure {
                logger.severe("WILDFLY-DEPLOY-ERROR-0010: There was an error logging out of the management API")
                throw it
            }

            return this
        }
    }

    fun shutdown(): WildflyService {
        synchronized(jbossCli) {
            Try{retry.execute(RetryCallback<Unit, Throwable> { context ->
                checkState(!connected.get(), "You must disconnect before terminating")

                logger.info("Attempt ${context.retryCount + 1} to terminate.")

                jbossCli.terminate()

                connected.set(false)
            })}
            .onFailure {
                logger.severe("WILDFLY-DEPLOY-ERROR-0011: There was an error terminating the CLI object")
                throw it
            }

            return this
        }
    }

    fun takeSnapshot(): Try<CLI.Result>  {
        return runCommandExpectSuccess(
                "/:take-snapshot",
                "take configuration snapshot",
                "WILDFLY-DEPLOY-ERROR-0001: There was an error taking a snapshot of the current configuration")
    }

    fun runCommand(command:String, description:String): Try<CLI.Result> {
        synchronized(jbossCli) {
            return Try{retry.execute(RetryCallback<CLI.Result, Throwable> { context ->
                checkState(connected.get(), "You must be connected before running commands")

                logger.info("Attempt ${context.retryCount + 1} to $description.")

                val result = jbossCli.cmd(command)

                logger.info("Command: " + command)
                logger.info("Result as JSON: " + result?.response?.toJSONString(false))

                result
            })}
        }
    }

    fun runCommandExpectSuccess(command:String, description:String, errorMessage:String): Try<CLI.Result> {
        synchronized(jbossCli) {
            return Try{retry.execute(RetryCallback<CLI.Result, Throwable> { context ->
                checkState(connected.get(), "You must be connected before running commands")

                logger.info("Attempt ${context.retryCount + 1} to $description.")

                val result = jbossCli.cmd(command)

                logger.info("Command: " + command)
                logger.info("Result as JSON: " + result?.response?.toJSONString(false))

                if (!result.isSuccess) {
                    throw Exception(errorMessage)
                }

                result
            })}
        }
    }
}