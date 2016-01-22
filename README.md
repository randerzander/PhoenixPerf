**Build**
```
mvn clean package
```

Test configuration is specified using the 'props' Java properties file.

Make sure to define your table before running and update the upsert statement logic generation if you use a table definition different than the below.

**Example DDL**
```
create table test(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  val bigint,
  constraint my_pk primary key (cust_id, process_id, doc_id)
);
```

**Using sqlline to create the test table**

If ZooKeeper is not running on 'localhost', use the fqdn of one of your hosts actually running ZooKeeper server

```
bash-4.1# /usr/hdp/current/phoenix-client/bin/sqlline.py localhost:2181:/hbase-unsecure
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix:localhost:2181:/hbase-unsecure none none org.apache.phoenix.jdbc.PhoenixDriver
Connecting to jdbc:phoenix:localhost:2181:/hbase-unsecure
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/usr/hdp/2.3.4.0-3485/phoenix/phoenix-4.4.0.2.3.4.0-3485-client.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/usr/hdp/2.3.4.0-3485/hadoop/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
16/01/22 09:38:36 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
16/01/22 09:38:37 WARN shortcircuit.DomainSocketFactory: The short-circuit local reads feature cannot be used because libhadoop cannot be loaded.
Connected to: Phoenix (version 4.4)
Driver: PhoenixEmbeddedDriver (version 4.4)
Autocommit status: true
Transaction isolation: TRANSACTION_READ_COMMITTED
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
87/87 (100%) Done
Done
sqlline version 1.1.8
0: jdbc:phoenix:localhost:2181:/hbase-unsecur> drop table test;
No rows affected (3.779 seconds)
0: jdbc:phoenix:localhost:2181:/hbase-unsecur> create table test(
. . . . . . . . . . . . . . . . . . . . . . .>   cust_id integer not null,
. . . . . . . . . . . . . . . . . . . . . . .>   process_id integer not null,
. . . . . . . . . . . . . . . . . . . . . . .>   doc_id integer not null,
. . . . . . . . . . . . . . . . . . . . . . .>   val bigint,
. . . . . . . . . . . . . . . . . . . . . . .>   constraint my_pk primary key (cust_id, process_id, doc_id)
. . . . . . . . . . . . . . . . . . . . . . .> );
No rows affected (1.322 seconds)
0: jdbc:phoenix:localhost:2181:/hbase-unsecur>
```

**Run Performance Test**
```
java -jar target/perf-1.0-SNAPSHOT.jar props
```

**Sample Output, 10 Threads with 10 repetitions writing 10k records, committing every 5 writes**
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
