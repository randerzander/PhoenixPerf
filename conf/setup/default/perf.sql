CREATE TABLE IF NOT EXISTS perf(
 testName varchar not null,
 metric varchar not null,
 threadId integer not null,
 rep integer not null,
 ts timestamp not null,
 val double,
 CONSTRAINT my_pk PRIMARY KEY (testName, metric, threadId, rep, ts)
);
