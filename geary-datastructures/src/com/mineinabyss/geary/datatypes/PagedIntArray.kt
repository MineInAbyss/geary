package com.mineinabyss.geary.datatypes

private const val PAGE_SIZE = 4096

/**
 * An integer array, broken up into pages of [PAGE_SIZE].
 *
 * The underlying structure keeps an array of [pages], with unfilled pages pointing to an [emptyArray] element,
 * once an insertion happens, the empty page is replaced with an array of [PAGE_SIZE], with the pages array being
 * replaced
 */
class PagedIntArray {
    private val emptyArray = IntArray(0)
    private var pages = Array(16) { emptyArray }
    private var maxSupportedSize = pages.size * PAGE_SIZE

    operator fun get(index: Int): Int {
        val bucketIndex = index / PAGE_SIZE
        val bucket = pages[bucketIndex]
        if (bucket === emptyArray) return 0
        return bucket[index % PAGE_SIZE]
    }

    operator fun set(index: Int, value: Int) {
        val bucketIndex = index / PAGE_SIZE
        ensureSize(index + 1)
        val page = getOrCreatePage(bucketIndex)
        page[index % PAGE_SIZE] = value
    }

    fun remove(index: Int) {
        set(index, 0)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun ensureSize(including: Int) {
        if (including > maxSupportedSize) {
            val requiredSize = maxOf(including, maxSupportedSize * 3 / 2) // grow by 1.5
            val newPageCount = (requiredSize + PAGE_SIZE - 1) / PAGE_SIZE // round up
            pages = pages.copyOf(newPageCount) { emptyArray }
            maxSupportedSize = pages.size * PAGE_SIZE
        }
    }

    private fun getOrCreatePage(index: Int): IntArray {
        val page = pages[index]
        if (page === emptyArray) {
            val newPage = IntArray(PAGE_SIZE)
            pages[index] = newPage
            return newPage
        }
        return page
    }
}
