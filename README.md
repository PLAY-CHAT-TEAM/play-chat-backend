
### Mysql 컨테이너 생성 및 실행(백그라운드)
```shell
docker run --name playchat-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=pass -d mysql:8.0.28
```
### 실행중인 Mysql 컨테이너에 접속
```shell
docker exec -it playchat-mysql bash
```

### 컨테이너 접속후 DB 생성하기
```shell
root/# mysql -uroot -ppass
mysql> create schema playchat;
```
