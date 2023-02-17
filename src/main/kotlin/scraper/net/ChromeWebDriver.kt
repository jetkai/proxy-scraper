package scraper.net

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.Command
import org.openqa.selenium.devtools.DevTools
import org.openqa.selenium.devtools.v109.network.Network

/**
 * ChromeWebDriver - 12/02/2023
 * @author Kai
 *
 * Description: Used for more complex proxy sites, ones that require JavaScript & obfuscate their data
 **/
class ChromeWebDriver {

    private val options = ChromeOptions()
    private var driver : ChromeDriver ? = null
    private var devTools : DevTools? = null
    private var responseBody : String? = null

    init { createInstance() }

    private fun createInstance() {
        //Create ObjectMapper
        val mapper = ObjectMapper()
        //Config Options
        options.addArguments("-headless", "start-maximized")
        //Init Driver
        driver = ChromeDriver(options)
        //Create DevTools Session
        devTools = driver?.devTools
        devTools?.createSession()
        devTools?.send(Command<Any>("Network.enable", ImmutableMap.of()))
        //Adds a listener to read all data received, parse json from network log for proxies (if there is no public api)
        devTools?.addListener(Network.dataReceived()) { received ->
            val networkResponseBody = Network.getResponseBody(received.requestId)
            val body = devTools?.send(networkResponseBody)?.body
            if(body != null && received.dataLength > 0) {
                try {
                    mapper.readValue<Any>(body)
                    responseBody = body
                } catch (ex : JsonParseException) {
                    //Unable to parse because data is not JSON
                }
            }
        }
    }

    suspend fun browse(url : String) : String? {
        val driver = driver ?: return null
        //Ensure the browser is fully maximized
        driver.manage().window().maximize()
        return withContext(Dispatchers.IO) {
            //Browse the URL
            driver.get(url)
            //Close+quit driver
            driver.close()
            driver.quit()
            //Finally return responseBody
            responseBody
        }
    }

}