import random
import string

filename = "igendata.sql"

ord_sql = "INSERT INTO Orders VALUES ({}, '{}', {}, '{}');"
inv_sql = "INSERT INTO INVENTORY VALUES ({}, '{}', {}, {});"
staff_sql = "INSERT INTO STAFF VALUES ({}, '{}', '{}');"
ord_prod_sql = "INSERT INTO ORDER_PRODUCTS VALUES ({}, {}, {});"
delivery_sql = "INSERT INTO DELIVERIES VALUES ({}, '{}', '{}', '{}', '{}', '{}', '{}');"
collection_sql = "INSERT INTO COLLECTIONS VALUES ({}, '{}', '{}', '{}');"
staff_ord_sql = "INSERT INTO STAFF_ORDERS VALUES({}, {});"

def genName(n):
	if n < 1:
		return ''
	return random.choice(string.ascii_uppercase) + ''.join(random.choices(string.ascii_lowercase, k = n - 1))

monthdays = {
	"Jan": 	31, 
	"Feb": 	28, 
	"Mar": 	31,
	"Apr": 	30,
	"May": 	31,
	"Jun": 	30,
	"Jul": 	31,
	"Aug": 	31,
	"Sep": 	30, 
	"Oct": 	31, 
	"Nov": 	30, 
	"Dec": 	31
}

months = list(monthdays.keys())

types = ["InStore", "Collection", "Delivery"]

# february only has 28 days periodt.
def genDate():
	minyear = 2001
	maxyear = 2020
	m = random.choice(months)
	y = random.randint(minyear, maxyear)
	d = random.randint(1,monthdays[m])
	return f"{str(d).zfill(2)}-{m}-{y}"

sql = ''

order_total = 300
collections_total = 100
deliveries_total = 100
staff_total = 10
inv_total = 100
order_products_total = 100


collection_id_start = 100
collection_id_end = 199
deliveries_id_start = 200
deliveries_id_end = 299

# STAFF
for i in range(staff_total):
	sql += staff_sql.format(i, genName(10), genName(10)) + '\n'

# INVENTORY
for i in range(inv_total):
	sql += inv_sql.format(i, genName(15), random.randint(5,5000), random.randint(200,500)) + '\n'

# ORDERS
for i in range(order_total):
	ordtype = types[int(i / 100) % 3]
	if ordtype == types[0]:
		completed = 1
	else:
		completed = random.randint(0,1)
	sql += ord_sql.format(i, ordtype, completed, genDate()) + '\n'

# COLLECTIONS
for i in range(collection_id_start, collection_id_end + 1):
	sql += collection_sql.format(i, genName(10), genName(10), genDate()) + '\n'

# DELIVERIES
for i in range(deliveries_id_start, deliveries_id_end + 1):
	sql += delivery_sql.format(i, genName(10), genName(10), genName(5) + " House", genName(5) + " St", genName(5) + " City", genDate()) + '\n'

# STAFF_ORDERS
for i in range(order_total):
	staffid = random.randint(0,staff_total - 1)
	sql += staff_ord_sql.format(staffid, i) + '\n'

# ORDER_PRODUCTS
for i in range(order_total):
	productcount = random.randint(1,10)
	productsbought = random.sample(range(inv_total), k = productcount)
	while productcount > 0:
		item = productsbought.pop()
		count = random.randint(1,productcount)
		sql += ord_prod_sql.format(i,item,count) + '\n'
		productcount -= count

print(sql)

with open(filename, 'w') as f:
	f.write(sql)