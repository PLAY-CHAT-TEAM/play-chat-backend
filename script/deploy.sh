#!/bin/bash

REPOSITORY=/home/ec2-user/app
PROJECT_NAME=play-chat


echo "> Git Pull"
cd $REPOSITORY/$PROJECT_NAME/
git pull


echo "> 프로젝트 Build 시작"
./gradlew build


echo "> Build 파일 복사"
cp $REPOSITORY/$PROJECT_NAME/build/libs/*.jar $REPOSITORY/


CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)
echo " 현재 구동 중인 애플리케이션 pid : $CURRENT_PID"


if [ -z "$CURRENT_PID" ]; then
	echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
	echo "> kill -15 $CURRENT_PID"
	kill -15 $CURRENT_PID
	sleep 5
fi


JAR_NAME=$(ls -tr $REPOSITORY/ | grep jar | tail -n 1)
cd $REPOSITORY
echo "> 새 애플리케이션 배포 [ JAR Name: $JAR_NAME ]"
nohup java -jar \
        -Dspring.profiles.active=prod \
        $REPOSITORY/$JAR_NAME 2>&1 &
