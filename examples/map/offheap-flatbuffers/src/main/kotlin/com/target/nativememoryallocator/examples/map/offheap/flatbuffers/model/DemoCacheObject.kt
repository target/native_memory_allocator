package com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model

/**
 * A demo cache object containing an [id] field and a list of entries.
 */
data class DemoCacheObject(
    val id: Int,
    val entryList: List<DemoCacheObjectListEntry>,
)

/**
 * A demo cache object list entry.
 */
data class DemoCacheObjectListEntry(
    val id: Int,
    val booleanField: Boolean,
    val stringField: String,
)