package com.congdanh.compositebuild

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

/**
 * Get libs version catalog.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Get a library from the version catalog.
 */
internal operator fun VersionCatalog.get(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

/**
 * Get a version from the version catalog.
 */
internal fun VersionCatalog.version(alias: String): VersionConstraint =
    findVersion(alias).get()

/**
 * Get a plugin from the version catalog.
 */
internal fun VersionCatalog.plugin(alias: String): PluginDependency =
    findPlugin(alias).get().get()
