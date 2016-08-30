drop table if exists test;

create table test(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  val bigint,
  constraint my_pk primary key (cust_id, process_id, doc_id)
);
