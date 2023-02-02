# benchmarks

Benchmarks of NMA and RocksDB using [JMH](https://github.com/openjdk/jmh)

Based on ideas from [Caffeine's JMH benchmarks](https://github.com/ben-manes/caffeine/tree/master/caffeine/src/jmh/java/com/github/benmanes/caffeine), especially [GetPutBenchmark](https://github.com/ben-manes/caffeine/blob/master/caffeine/src/jmh/java/com/github/benmanes/caffeine/cache/GetPutBenchmark.java)

Command to run all benchmarks from top-level directory:

```
./gradlew clean jmh
```

Command to build standalone fat-jar of benchmarks:

```
./gradlew clean jmhJar
```