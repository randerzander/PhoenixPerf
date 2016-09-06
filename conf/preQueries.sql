drop table if exists test;
create table if not exists test(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  val bigint,
  constraint my_pk primary key (cust_id, process_id, doc_id)
);

drop table if exists perf;
create table if not exists perf(
 testName varchar not null,
 metric varchar not null,
 threadId integer not null,
 rep integer not null,
 ts timestamp not null,
 val double,
 constraint my_pk primary key (testName, metric, threadId, rep, ts)
);
