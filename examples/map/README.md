# map examples

## OnHeapDemo

* Put 20,000 CacheObject instances into a ConcurrentHashMap.
* Each CacheObject instance contains a random string of length 500KB.
* This is a total of 10 GB of data stored on the normal Java heap.
* This application does not make use of any features of the native_memory_allocator library.  It is provided for comparison with NativeMemoryMap demos using off-heap storage.

## OffHeapDemo

* Put 20,000 CacheObject instances into a NativeMemoryMap.
* This demo uses the ConcurrentHashMap backend for NativeMemoryMap, which does not support eviction.
* Each CacheObject instance contains a random string of length 500KB.
* This is a total of 10 GB of data in off-heap memory.

## OffHeapDemoWithEviction

* Same as OffHeapDemo but uses the Caffeine backend with maximumSize of 10,000 entries.
* Expect 10,000 entries at end of run with 10,000 total evictions.