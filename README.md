# http client
a http client to send request as fast as possible 
use netty async model
## use method
 -d <host>              dst_host
 -f <f>                 urlfile
 -n <uselocalportnum>   uselocalportnum
 -p <port>              dst_port
 -t <thead_num>         thead_num
### example 
    java -jar ../httpclient-0.0.1-SNAPSHOT.jar  -f test.url -d 127.0.0.1  -p 80 -n 20000 -t 8
    test.url is a file every line is a url
