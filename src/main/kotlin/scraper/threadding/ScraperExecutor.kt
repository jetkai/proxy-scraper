package scraper.threadding

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ScraperExecutor {

    private val PROCESSORS = Runtime.getRuntime().availableProcessors() + 1

    private val scraperWorker : ExecutorService = Executors.newFixedThreadPool(PROCESSORS, ScraperThreadFactory("Scraper-Worker"))

    fun submitTask(task : Runnable) {
        scraperWorker.submit(task)
    }

}