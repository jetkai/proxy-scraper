package scraper.plugin

import mu.KotlinLogging
import org.reflections.Reflections
import scraper.plugin.hook.ProxyWebsite
import java.lang.reflect.Modifier

object PluginFactory {

    private val logger = KotlinLogging.logger { }

    private val plugins : MutableMap<String, Plugin> = mutableMapOf()
    val proxyWebsites : MutableList<ProxyWebsite> = mutableListOf()

    fun init() {
        logger.info { "Loading Plugin Factory..." }
        val pluginClasses : Set<Class<out Plugin>> = Reflections("plugin")
            .getSubTypesOf(Plugin::class.java)
        for (classz in pluginClasses) {
            if (Modifier.isAbstract(classz.modifiers)) {
                continue
            }
            try {
                val instance: Plugin = classz.getDeclaredConstructor().newInstance()
                instance.register()
                plugins[classz.name] = instance
            } catch (t : Throwable) {
                logger.error {"Failed to initialize website plugin: ${classz.simpleName}." }
            }
        }
        logger.info { "Loaded " + plugins.size + " website plugins." }
    }

    fun register(proxyWebsite : ProxyWebsite) {
        proxyWebsites.add(proxyWebsite)
    }

}