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

**Run Performance Test**
```
#See mixed-test.props for comments on the function of each property.
java -jar target/perf-1.0-SNAPSHOT.jar mixed-test.props
```

**Sample Output, 5 write and 5 read threads, 10 repetitions writing 1k count per rep, committing every 5 writes**

The below was generated using the included 'props' file on a 2014 MacBook Pro. With simultaneous reads & writes, a single-node achieved about 650 upserts/sec and 440 queries per sec.
```
WRITE THREAD 2 rep 8 of 10: 1000 / 1.376693614 = 726 upserts/sec
WRITE THREAD 1 rep 8 of 10: 1000 / 1.381533831 = 724 upserts/sec
WRITE THREAD 4 rep 8 of 10: 1000 / 1.393602211 = 718 upserts/sec
WRITE THREAD 0 rep 8 of 10: 1000 / 1.44201247 = 693 upserts/sec
WRITE THREAD 3 rep 8 of 10: 1000 / 1.402683061 = 713 upserts/sec
WRITE THREAD 1 rep 9 of 10: 1000 / 1.170773649 = 854 upserts/sec
WRITE THREAD 2 rep 9 of 10: 1000 / 1.290999103 = 775 upserts/sec
WRITE THREAD 4 rep 9 of 10: 1000 / 1.213860169 = 824 upserts/sec
WRITE THREAD 3 rep 9 of 10: 1000 / 1.136755893 = 880 upserts/sec
WRITE THREAD 0 rep 9 of 10: 1000 / 1.230584087 = 813 upserts/sec
READ THREAD 1 rep 4 of 10: 1000 / 2.324400585 = 430 qps
READ THREAD 4 rep 4 of 10: 1000 / 2.146260529 = 466 qps
READ THREAD 3 rep 4 of 10: 1000 / 1.993750249 = 502 qps
READ THREAD 2 rep 4 of 10: 1000 / 2.089134783 = 479 qps
READ THREAD 0 rep 4 of 10: 1000 / 2.080175987 = 481 qps
READ THREAD 1 rep 5 of 10: 1000 / 1.265383209 = 790 qps
READ THREAD 4 rep 5 of 10: 1000 / 1.277487047 = 783 qps
READ THREAD 0 rep 5 of 10: 1000 / 1.128897183 = 886 qps
READ THREAD 3 rep 5 of 10: 1000 / 1.315074737 = 760 qps
READ THREAD 2 rep 5 of 10: 1000 / 1.235333713 = 809 qps
READ THREAD 0 rep 6 of 10: 1000 / 0.99553133 = 1004 qps
READ THREAD 1 rep 6 of 10: 1000 / 1.185390286 = 844 qps
READ THREAD 3 rep 6 of 10: 1000 / 1.021614066 = 979 qps
READ THREAD 2 rep 6 of 10: 1000 / 1.096592781 = 912 qps
READ THREAD 4 rep 6 of 10: 1000 / 1.200709482 = 833 qps
READ THREAD 0 rep 7 of 10: 1000 / 2.232705343 = 448 qps
WRITE DONE: 1: 10000 / 15.222851002 = 657 upserts/sec
READ THREAD 3 rep 7 of 10: 1000 / 2.229323884 = 449 qps
WRITE DONE: 2: 10000 / 15.285020921 = 654 upserts/sec
READ THREAD 1 rep 7 of 10: 1000 / 2.255869767 = 443 qps
WRITE DONE: 4: 10000 / 15.311429168 = 653 upserts/sec
READ THREAD 2 rep 7 of 10: 1000 / 2.228224597 = 449 qps
WRITE DONE: 3: 10000 / 15.39989397 = 649 upserts/sec
WRITE DONE: 0: 10000 / 15.42180439 = 648 upserts/sec
READ THREAD 4 rep 7 of 10: 1000 / 2.328714532 = 429 qps
READ THREAD 0 rep 8 of 10: 1000 / 1.017816904 = 982 qps
READ THREAD 1 rep 8 of 10: 1000 / 1.061889944 = 942 qps
READ THREAD 3 rep 8 of 10: 1000 / 1.136866062 = 880 qps
READ THREAD 2 rep 8 of 10: 1000 / 1.158624865 = 863 qps
READ THREAD 4 rep 8 of 10: 1000 / 1.111212913 = 900 qps
READ THREAD 0 rep 9 of 10: 1000 / 1.322511651 = 756 qps
READ THREAD 1 rep 9 of 10: 1000 / 1.25272783 = 798 qps
READ THREAD 3 rep 9 of 10: 1000 / 1.226200425 = 816 qps
READ THREAD 2 rep 9 of 10: 1000 / 1.137293052 = 879 qps
READ THREAD 4 rep 9 of 10: 1000 / 1.120653633 = 892 qps
READ DONE: 0: 10000 / 22.536284347 = 444 qps
READ DONE: 1: 10000 / 22.59488533 = 443 qps
READ DONE: 3: 10000 / 22.630177862 = 442 qps
READ DONE: 2: 10000 / 22.655865471 = 441 qps
READ DONE: 4: 10000 / 22.719553975 = 440 qps
```
