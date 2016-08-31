select count(*) from test;
select cust_id, count(*) from test group by cust_id;
select doc_id, count(*) from test group by doc_id;
select sum(val) from test group by doc_id;
select sum(val) from test group by cust_id;
