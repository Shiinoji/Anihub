package com.watchlist.anihub.ui

fun String?.cleanDescription(): String {
    if (this == null) return ""
    
    return this.replace(Regex("<br\\s*/?>"), "\n") // Handle line breaks
        .replace(Regex("<.*?>"), "") // Strip remaining HTML tags
        .replace(Regex("&quot;"), "\"")
        .replace(Regex("&amp;"), "&")
        .replace(Regex("&lt;"), "<")
        .replace(Regex("&gt;"), ">")
        .replace(Regex("&nbsp;"), " ")
        .replace(Regex("\\[(.*?)]\\(.*?\\)"), "$1") // Strip markdown links, keep text
        .replace(Regex("__(.*?)__"), "$1") // Bold/Italic markdown
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("~(.*?)~"), "$1")
        .trim()
}
