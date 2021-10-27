package com.target.oss.nativememoryallocator.map.impl

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import com.target.oss.nativememoryallocator.map.CaffeineConfigBuilder
import java.util.concurrent.TimeUnit

class CaffeineConfigBuilderImpl(
    var caffeine: Caffeine<Any, Any>,
) : CaffeineConfigBuilder {

    override fun expireAfterAccess(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder {
        caffeine = caffeine.expireAfterAccess(duration, timeUnit)
        return this
    }

    override fun maximumSize(maximumSize: Long): CaffeineConfigBuilder {
        caffeine = caffeine.maximumSize(maximumSize)
        return this
    }

    override fun recordStats(): CaffeineConfigBuilder {
        caffeine = caffeine.recordStats()
        return this
    }

    override fun ticker(ticker: Ticker): CaffeineConfigBuilder {
        caffeine = caffeine.ticker(ticker)
        return this
    }
}