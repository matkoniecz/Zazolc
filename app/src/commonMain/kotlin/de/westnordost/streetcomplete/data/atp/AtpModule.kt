package de.westnordost.streetcomplete.data.atp

import org.koin.dsl.module

val atpModule = module {
    factory { AtpDao(get()) }
    factory { AtpDownloader(get(), get()) }
    // TODO API: connect to actual bidirectional API
    val OSM_ATP_COMPARISON_API_BASE_URL = "https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/"
    factory { AtpApiClient(get(), OSM_ATP_COMPARISON_API_BASE_URL, get()) }
    factory { AtpApiParser() }

    single { AtpController(get()) }
}
