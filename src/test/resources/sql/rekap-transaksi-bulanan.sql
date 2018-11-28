select extract(year from transaction_time) as year,
       to_char(transaction_time,'MM') as month,
       count(id) as "# of Transaction",
       sum("amount") as "Total Amount"
from payment
group by 1,2
order by 1,2;