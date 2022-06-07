package com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model

data class DemoCacheObject(
    val id: Int,
    val entryList: List<DemoCacheObjectListEntry>,
)

data class DemoCacheObjectListEntry(
    val id: Int,
    val booleanField: Boolean,
    val stringField: String,
)