rootProject.name = "native_memory_allocator"

include(
    "examples",
    "examples:map",
    "examples:map:utils",
    "examples:map:onheapdemo",
    "examples:map:offheapdemo",
    "examples:map:offheapdemowitheviction",
    "examples:map:offheapdemowithevictionandoperationcounters",
)
