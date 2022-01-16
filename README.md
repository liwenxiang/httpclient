# http client
a http client to send request as fast as possible 
use netty async model
## use method
 -h <host>              dst_host
 -p <port>              dst_port
 -f <file>              inputfile
 -n <uselocalportnum>   uselocalportnum
 -q <qpslimit>          qpslimit
 -t <thead_num>         thead_num
 -fc <fileReuseCount>   fileReuseCount

### example 
    java -jar target/httpclient-0.0.1-SNAPSHOT.jar  -h www.baidu.com -f a.url -n 10 -p 80 -q 12 -t 2 -fc 3
    a.url is a file every line is a url
