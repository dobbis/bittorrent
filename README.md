## 컴파일 환경

java version "1.8.0_201"

Java(TM) SE Runtime Environment (build 1.8.0_201-b09)

Java HotSpot(TM) 64-Bit Server VM (build 25.201-b09, mixed mode)

javac “1.8.0_201”

## 컴파일 방법

1. Makefile이 존재하는 directory로 이동
2. make all 명령어 입력(워닝이 뜨지만 무시)
3. bin directory로 이동
4. bin directory안에 configuration.txt을 작성한다
5. server_file에 다운로드할 파일을 넣는다
6. java bittorrent.Main명령어 입력
7. client_file에 client의 port number로 된 directory가 생성된다
8. 각각의 directory안에 다운로드 받은 파일이 생성된다

## Configuration files format

- 파일명 : configuration.txt
- 파일 위치 : Main과 같은 디렉토리
- 파일 내용 :
    1. client1의 IP address
    2. client1의 port number
    3. client2의 IP address
    4. client2의 port number
    5. client3의 IP address
    6. client3의 port number
    7. download할 파일의 pathname
    8. server의 port number