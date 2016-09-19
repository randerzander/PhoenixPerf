DROP TABLE IF EXISTS documents;

CREATE TABLE IF NOT EXISTS documents(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  doc varchar,
  CONSTRAINT my_pk PRIMARY KEY (cust_id, process_id, doc_id)
);
