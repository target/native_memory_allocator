rootProject.name = "native_memory_allocator"

include(
    "examples",
    "examples:map",
    "examples:map:utils",
    "examples:map:onheap",
    "examples:map:offheap",
    "examples:map:offheap-eviction",
    "examples:map:offheap-eviction-operationcounters",
)
