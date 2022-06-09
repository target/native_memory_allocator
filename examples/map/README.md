# map examples

## running examples

All examples have the gradle `shadowJar` plugin configured to easily run as a standalone app:

```
cd examples/map/offheap
java -jar build/libs/*shadowjar*.jar
```

## onheap example

* Put 20,000 CacheObject instances into a ConcurrentHashMap.
* Each CacheObject instance contains a random string of length 500KB.
* This is a total of 10GB of data stored on the normal Java heap.
* This application does not make use of any features of the native_memory_allocator library. It is provided for
  comparison with NativeMemoryMap demos using off-heap storage.
* As this application uses > 10GB of Java heap space, you may have to increase the Java heap size (e.g. `-Xmx20g`).

## offheap example

* Same as onheap but uses NativeMemoryMap with off-heap storage.
* Uses the ConcurrentHashMap backend for NativeMemoryMap, which does not support eviction.
* Total of 10GB of data in off-heap memory.
* At end of test after full GC uses ~130MB of Java heap space.

## offheap-eviction example

* Same as offheap but uses the Caffeine backend with maximumSize of 10,000 entries.
* Expect 10,000 map entries at end of run with 10,000 total evictions.
* Total of 5GB of data in off-heap memory.
* At end of test after full GC uses ~90MB of Java heap space.

## offheap-eviction-operationcounters example

* Same as offheap-eviction but enables operation counters and logs them at the end of the demo.
* Total of 5GB of data in off-heap memory.
* At end of test after full GC uses ~90MB of Java heap space.

## offheap-flatbuffers example

* Example using [Google FlatBuffers](https://google.github.io/flatbuffers/) for serialization.
* Put 20,000 DemoCacheObject instances into a NativeMemoryMap. Each DemoCacheObject contains a list of 2,000
  DemoCacheObjectListEntry objects.
* Total of ~11GB of data in off-heap memory.
* DemoCacheObjectSerializer does conversions between DemoCacheObject and FlatBuffer objects.
* At end of test after full GC uses ~130MB of Java heap space.