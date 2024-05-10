nohup java -Xms1024m -Xmx2048m \
 -Dshort-link.goto-domain.white-list.enable=false \
 -Dshort-link.domain.default=47.98.138.232:8003 \
 -jar /home/lyl/shortlink/gateway-all.jar > /usr/lyl/shortlink/logs/shortlink-aggregation.file 2>&1 &



nohup java -Xms1024m -Xmx1024m \
 -jar /home/lyl/shortlink/aggregation-all.jar > /usr/lyl/shortlink/logs/shortlink-gateway.file 2>&1 &


curl --location --request GET 'http://47.98.138.232:80/api/short-link/admin/v1/user/user3' \
--header 'token: 4b312f0b-aa6a-474a-8c20-70b2544c8f97' \
--header 'username: admin' \
--header 'User-Agent: Apifox/1.0.0 (https://apifox.com)'