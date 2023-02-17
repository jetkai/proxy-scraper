package scraper.threading

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class ScraperThreadFactory(private val name : String) : ThreadFactory {
    private val threadCount = AtomicInteger()
    override fun newThread(r : Runnable): Thread {
        return Thread(r, name + "-" + threadCount.getAndIncrement())
    }
}
