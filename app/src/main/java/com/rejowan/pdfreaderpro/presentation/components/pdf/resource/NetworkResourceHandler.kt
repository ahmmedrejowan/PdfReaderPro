package com.bhuvaneshw.pdf.resource

import java.net.HttpURLConnection
import java.net.URL

/**
 * An interface for handling network resource requests.
 */
interface NetworkResourceHandler {
    /**
     * Opens a network resource from the given URL.
     *
     * @param url The URL of the resource to open.
     * @return A [NetworkResource] representing the opened resource.
     */
    fun open(url: String): NetworkResource
}

/**
 * A default implementation of [NetworkResourceHandler] that uses [HttpURLConnection].
 *
 * @param beforeConnect A lambda that can be used to customize the [HttpURLConnection] before connecting.
 */
class DefaultNetworkResourceHandler(
    private val beforeConnect: (HttpURLConnection.() -> Unit)? = null,
) : NetworkResourceHandler {

    override fun open(url: String): NetworkResource {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection

        beforeConnect?.invoke(connection)
        connection.connect()

        return NetworkResource(
            mimeType = connection.contentType ?: "application/octet-stream",
            encoding = connection.contentEncoding ?: "UTF-8",
            inputStream = connection.inputStream,
        )
    }

}
