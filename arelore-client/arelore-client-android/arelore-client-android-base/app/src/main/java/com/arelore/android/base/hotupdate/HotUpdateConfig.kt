package com.arelore.android.base.hotupdate

/**
 * Remote hot-update config payload.
 *
 * Backend can publish JSON like:
 * {
 *   "contentUrl": "http://www.arelore.com/home",
 *   "version": "20260323.1",
 *   "forceRefresh": true
 * }
 *
 * Changing this JSON updates in-app content without releasing a new APK.
 */
data class HotUpdateConfig(
    val contentUrl: String,
    val version: String? = null,
    val forceRefresh: Boolean = false
)
