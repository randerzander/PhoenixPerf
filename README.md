**Build**
```
mvn clean package
```

Test configuration is specified using the 'test.props' Java properties file.

To run a series of prep queries defined in a file, point the "preQueryFile" prop to it.

Make sure to define your table before running and update the upsert statement logic generation if you use a table definition different than the below.

**Example DDL**
```
$> cat preQueries.sql
drop table if exists test;
create table test(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  val bigint,
  constraint my_pk primary key (cust_id, process_id, doc_id)
);
```

**Using sqlline to create the test table**

If ZooKeeper is not running on 'localhost', use the fully qualified domain name of one of your hosts running a ZooKeeper server instance

**Run Performance Test**
```
java -jar target/perf-1.0-SNAPSHOT.jar test.props
```

**Sample Output, 10 Threads with 10 repetitions writing 10k records, committing every 5 writes**

The below was generated using the included 'props' file on a 10 year old PowerEdge 1950 with 4 cores and 32GB RAM.

With 10 threads, committing every 5 records, the apparent aggregate write-rate is about 10k/sec.
```
THREAD 8 rep 8 of 10: 10000 / 7.619866212 = 1312.358999722553
THREAD 0 rep 8 of 10: 10000 / 7.626117367 = 1311.2832544739408
THREAD 6 rep 8 of 10: 10000 / 7.600011494 = 1315.7874837287713
THREAD 3 rep 8 of 10: 10000 / 7.579436939 = 1319.3592189605788
THREAD 2 rep 8 of 10: 10000 / 7.612197026 = 1313.6811837429182
THREAD 4 rep 8 of 10: 10000 / 7.611259816 = 1313.8429434478787
THREAD 5 rep 8 of 10: 10000 / 7.622023349 = 1311.9875841513904
THREAD 7 rep 8 of 10: 10000 / 7.676222231 = 1302.7241394361347
THREAD 1 rep 9 of 10: 10000 / 7.856362998 = 1272.8536095577188
THREAD 0 rep 9 of 10: 10000 / 7.795843603 = 1282.7348147610087
THREAD 8 rep 9 of 10: 10000 / 7.811032197 = 1280.2405300340104
THREAD 9 rep 9 of 10: 10000 / 7.822039608 = 1278.4389367924562
THREAD 6 rep 9 of 10: 10000 / 7.812644346 = 1279.9763507883097
THREAD 3 rep 9 of 10: 10000 / 7.810771386 = 1280.283278796761
THREAD 2 rep 9 of 10: 10000 / 7.803939991 = 1281.4040102220977
THREAD 4 rep 9 of 10: 10000 / 7.799588775 = 1282.1188768378368
THREAD 5 rep 9 of 10: 10000 / 7.767940092 = 1287.3425749380767
THREAD 7 rep 9 of 10: 10000 / 7.639059383 = 1309.0616918431147
DONE: 1: 100000 / 95.245927626 = 1049.9136550243672
DONE: 0: 100000 / 95.298194457 = 1049.3378239723265
DONE: 8: 100000 / 95.307640185 = 1049.2338264371224
DONE: 9: 100000 / 95.309206742 = 1049.2165806258138
DONE: 6: 100000 / 95.329534528 = 1048.992848807294
DONE: 3: 100000 / 95.349075126 = 1048.7778708692663
DONE: 2: 100000 / 95.35425241 = 1048.720927201279
DONE: 4: 100000 / 95.353370193 = 1048.730630051093
DONE: 5: 100000 / 95.370020974 = 1048.5475307514323
DONE: 7: 100000 / 95.392204617 = 1048.30368898067
```
