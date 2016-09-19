DROP TABLE IF EXISTS documents;

CREATE TABLE IF NOT EXISTS documents(
  cust_id integer not null,
  process_id integer not null,
  doc_id integer not null,
  doc varchar,
  CONSTRAINT my_pk PRIMARY KEY (cust_id, process_id, doc_id)
);

DROP INDEX IF EXISTS process_id_idx ON documents;
CREATE INDEX IF NOT EXISTS process_id_idx ON documents (process_id) include (cust_id, doc_id);

DROP INDEX IF EXISTS doc_id_idx ON documents;
CREATE INDEX IF NOT EXISTS doc_id_idx ON documents (doc_id) include (cust_id, process_id);
