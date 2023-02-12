package scraper.net

import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxBinary
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions

class FireFoxWebDriver {

    private val firefoxBinary = FirefoxBinary()
    private val firefoxOptions = FirefoxOptions()
    private var firefoxDriver : WebDriver ? = null

    init {
        buildDriver()
    }

    private fun buildDriver() {
        firefoxOptions.binary = firefoxBinary
        firefoxOptions.addArguments("-headless")
        firefoxOptions.addArguments("--window-size=1920,1080")
        firefoxOptions.addArguments("--width=1920")
        firefoxOptions.addArguments("--height=1080")
        firefoxDriver = FirefoxDriver(firefoxOptions)
    }

    fun instance() : WebDriver? {
        return firefoxDriver
    }

}