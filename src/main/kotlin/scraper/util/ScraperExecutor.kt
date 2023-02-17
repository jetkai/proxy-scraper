package scraper.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * ScraperExecutor - 17/02/2023
 * @author Kai
 *
 * Description: This is used for multi-threading to speed up the application
 **/
object ScraperExecutor {

    private val PROCESSORS = Runtime.getRuntime().availableProcessors() + 1

    private val scraperWorker : ExecutorService = Executors.newFixedThreadPool(PROCESSORS, ScraperThreadFactory("Scraper-Worker"))

    fun submitTask(task : Runnable) {
        scraperWorker.submit(task)
    }

}