package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.map.NativeMemoryMapOperationCounters
import java.util.concurrent.atomic.AtomicLong

class NativeMemoryMapOperationCountersImpl(
    override val numPutsNoChanges: AtomicLong = AtomicLong(0),
    override val numPutsFreedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsReusedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsNewBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesFreedBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesNoChange: AtomicLong = AtomicLong(0),
    override val numGetsNullValue: AtomicLong = AtomicLong(0),
    override val numGetsNonNullValue: AtomicLong = AtomicLong(0),
) : NativeMemoryMapOperationCounters