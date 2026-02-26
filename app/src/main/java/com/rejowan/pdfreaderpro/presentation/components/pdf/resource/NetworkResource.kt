package com.bhuvaneshw.pdf.resource

import java.io.InputStream

/**
 * Represents a resource fetched from the network.
 *
 * @property mimeType The MIME type of the resource.
 * @property encoding The encoding of the resource.
 * @property inputStream The input stream of the resource.
 */
data class NetworkResource(
    val mimeType: String,
    val encoding: String,
    val inputStream: InputStream,
)
