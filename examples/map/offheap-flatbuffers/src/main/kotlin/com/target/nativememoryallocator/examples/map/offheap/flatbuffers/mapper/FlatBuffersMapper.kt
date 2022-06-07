package com.target.nativememoryallocator.examples.map.offheap.flatbuffers.mapper

import com.google.flatbuffers.FlatBufferBuilder
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.generated.FlatBufferDemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.generated.FlatBufferDemoCacheObjectListEntry
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObjectListEntry
import java.nio.ByteBuffer

object FlatBuffersMapper {

    fun demoCacheObjectToFlatBuffer(
        demoCacheObject: DemoCacheObject,
    ): FlatBufferDemoCacheObject {
        val fbb = FlatBufferBuilder()
        fbb.forceDefaults(false)

        val entryListOffset = FlatBufferDemoCacheObject.createEntryListVector(
            builder = fbb,
            data = demoCacheObject.entryList.map { entry ->
                FlatBufferDemoCacheObjectListEntry.createFlatBufferDemoCacheObjectListEntry(
                    builder = fbb,
                    id = entry.id,
                    booleanField = entry.booleanField,
                    stringFieldOffset = fbb.createString(entry.stringField),
                )
            }.toIntArray(),
        )

        val flatBufferDemoCacheObject = FlatBufferDemoCacheObject.createFlatBufferDemoCacheObject(
            builder = fbb,
            id = demoCacheObject.id,
            entryListOffset = entryListOffset,
        )

        fbb.finish(flatBufferDemoCacheObject)

        return FlatBufferDemoCacheObject.getRootAsFlatBufferDemoCacheObject(
            ByteBuffer.wrap(
                fbb.sizedByteArray(),
            )
        )
    }

    fun flatBufferToDemoCacheObject(
        flatBufferDemoCacheObject: FlatBufferDemoCacheObject,
    ): DemoCacheObject {

        val entryList = (0 until flatBufferDemoCacheObject.entryListLength).mapNotNull { i ->
            val flatBufferEntry = flatBufferDemoCacheObject.entryList(i)
            if (flatBufferEntry == null) {
                null
            } else {
                DemoCacheObjectListEntry(
                    id = flatBufferEntry.id,
                    booleanField = flatBufferEntry.booleanField,
                    stringField = flatBufferEntry.stringField.orEmpty(),
                )
            }
        }

        return DemoCacheObject(
            id = flatBufferDemoCacheObject.id,
            entryList = entryList,
        )
    }
}