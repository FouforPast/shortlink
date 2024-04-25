cd "D:\Program Files\nacos\nacos-server-2.2.3\bin\"
cd "E:\Program Files\nacos-server-2.1.1\nacos\bin"
startup.cmd -m standalone
cd "D:\Program Files (x86)\sentinel"
java -jar .\sentinel-dashboard-1.8.6.jar
cd "C:\Program Files\Redis"
redis-server.exe redis.windows.conf
mqnamesrv
mqbroker -n 127.0.0.1:9876 autoCreateTopicEnable=true