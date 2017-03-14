The goals of this project are:

1. Provide a sample Java application that aids in testing SQL query interfaces including..
  a. [Apache Phoenix](http://phoenix.apache.org/)/[Apache HBase](http://hbase.apache.org/)
  b. [Apache Calcite](https://calcite.apache.org/)
  c. [Apache Hive](http://hive.apache.org/)

##Build:##

**Build for Phoenix Thin JDBC Client**
```
mvn clean package
```

**Build for Phoenix Thick JDBC Client**
```
mvn clean package -P thick
```

**Build for Calcite Druid Adapter**
```
mvn clean package -P calcite
```

##Running Performance Tests:##

**Write Performance Test**
```
#See mixed-test.props for comments on the function of each property.
java -jar target/perf-1.0-SNAPSHOT.jar conf/tests/write.props
```

Write tests include population of a table with "random" values. Modify WriteThread.java as needed to control how this dummy data is generated.

**Read Performance Test**
```
#See mixed-test.props for comments on the function of each property.
java -jar target/perf-1.0-SNAPSHOT.jar conf/read.props
```

**Druid Read Performance Test**
```
java -jar target/perf-1.0-SNAPSHOT.jar conf/test/druid.props
```

**Simultaneous Read/Write Performance Test**
```
#See mixed-test.props for comments on the function of each property.
java -jar target/perf-1.0-SNAPSHOT.jar conf/tests/mixed-test.props
```

To run a series of prep statements, put them in files under the specified setupQueryDir. You can include multiple files and multiple statements (separated by ';') in this directory.

It's also useful to define a test table schema and indices here.

**Example DDL**
```
$> cat conf/setup/test.sql
CREATE TABLE IF NOT EXISTS test(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  doc varchar,
  constraint my_pk primary key (cust_id, process_id, doc_id)
);
CREATE INDEX IF NOT EXISTS process_id_index ON test (process_id) INCLUDE(cust_id, doc_id);
```

#ToDo:
1. More examples demonstrating differences between read/write performance for various primary key and indexing strategies
