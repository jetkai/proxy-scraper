package scraper.util

import java.util.regex.Pattern

/**
 * NetworkUtils - 17/02/2023
 * @author Kai
 *
 * Description: Any reusable network functions are in this class
 **/
object NetworkUtils {

    fun isValidIpAddress(ipAddress : String) : Boolean {
        val pattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$")
        return pattern.matcher(ipAddress).matches()
    }

    fun isValidIpAndPort(host : String): Boolean {
        val pattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}:\\d{1,5}\$")
        return pattern.matcher(host).matches()
    }

}