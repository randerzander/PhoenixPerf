select doc_id, count(*) from documents where cust_id < 400 group by doc_id
