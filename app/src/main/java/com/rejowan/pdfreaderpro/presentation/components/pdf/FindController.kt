package com.bhuvaneshw.pdf

import android.webkit.WebView
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke
import com.bhuvaneshw.pdf.js.toJsString

/**
 * Manages the find functionality within the PDF viewer.
 */
class FindController internal constructor(private val webView: WebView) {

    /**
     * Whether to highlight all occurrences of the search term.
     */
    var highlightAll: Boolean = true
        set(value) {
            field = value
            webView callDirectly "setFindHighlightAll"(value)
        }

    /**
     * Whether the search should be case-sensitive.
     */
    var matchCase: Boolean = false
        set(value) {
            field = value
            webView callDirectly "setFindMatchCase"(value)
        }

    /**
     * Whether to match whole words only.
     */
    var entireWord: Boolean = false
        set(value) {
            field = value
            webView callDirectly "setFindEntireWord"(value)
        }

    /**
     * Whether to match diacritics.
     */
    var matchDiacritics: Boolean = false
        set(value) {
            field = value
            webView callDirectly "setFindMatchDiacritics"(value)
        }

    /**
     * Starts a search for the given term.
     *
     * @param searchTerm The term to search for.
     */
    fun startFind(searchTerm: String) {
        webView callDirectly "startFind"(searchTerm.toJsString())
    }

    /**
     * Stops the current find operation.
     */
    fun stopFind() {
        webView callDirectly "stopFind"()
    }

    /**
     * Moves to the next search result.
     */
    fun findNext() {
        webView callDirectly "findNext"()
    }

    /**
     * Moves to the previous search result.
     */
    fun findPrevious() {
        webView callDirectly "findPrevious"()
    }

}
