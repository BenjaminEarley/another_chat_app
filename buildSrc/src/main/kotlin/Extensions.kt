import org.gradle.api.NamedDomainObjectContainer
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

fun <T> NamedDomainObjectContainer<T>.release(action: T.() -> Unit) {
    maybeCreate("release").action()
}

fun PluginDependenciesSpec.android(module: String): PluginDependencySpec =
    id("com.android.$module")

fun PluginDependenciesSpec.androidx(module: String): PluginDependencySpec =
    id("androidx.$module")