package com.watchlist.anihub.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicLong

/**
 * Interceptor to enforce a minimum delay between requests to throttle traffic.
 */
class RateLimitInterceptor(private val minDelayMs: Long = 200) : Interceptor {
    private val lastRequestTime = AtomicLong(0)

    override fun intercept(chain: Interceptor.Chain): Response {
        val now = System.currentTimeMillis()
        val lastTime = lastRequestTime.get()
        val elapsed = now - lastTime

        if (elapsed < minDelayMs) {
            Thread.sleep(minDelayMs - elapsed)
        }
        
        lastRequestTime.set(System.currentTimeMillis())
        return chain.proceed(chain.request())
    }
}
