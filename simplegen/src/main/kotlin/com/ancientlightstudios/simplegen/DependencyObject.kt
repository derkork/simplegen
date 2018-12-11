package com.ancientlightstudios.simplegen

data class DependencyObject<T>(val item:T, val lastModified:Long)

/**
 * @returns the time stamp of the latest modified dependency object in the list.
 */
fun Collection<DependencyObject<*>>.lastModified() : Long = this.map { it.lastModified }.max() ?: 0L
fun <T> Collection<DependencyObject<T>>.objects() = this.map { it.item }