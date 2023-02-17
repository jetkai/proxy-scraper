package scraper.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * ScraperThreadFactory - 12/02/2023
 * @author Kai
 *
 * Description: This is used for multi-threading to speed up the application
 **/
class ScraperThreadFactory(private val name : String) : ThreadFactory {
    private val threadCount = AtomicInteger()
    override fun newThread(r : Runnable): Thread {
        return Thread(r, name + "-" + threadCount.getAndIncrement())
    }
}
