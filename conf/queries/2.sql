select cust_id, count(*) from documents where cust_id > 100 and cust_id < 500 group by cust_id
