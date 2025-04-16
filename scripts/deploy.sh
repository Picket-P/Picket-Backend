#!/bin/bash

# 디버깅 정보
echo "Running deploy.sh as $(whoami)"
echo "Current directory: $(pwd)"
ls -l /home/ec2-user/

# 환경 변수 로드
if [ -f /home/ec2-user/.env ]; then
  echo "Loading environment variables from .env"
  set -a
  source /home/ec2-user/.env
  set +a
else
  echo "Error: .env file not found"
  exit 1
fi

# 기존 Spring 프로세스 종료
echo "Stopping existing Spring application..."
PID=$(pgrep -f 'java -jar /home/ec2-user/picket.jar')
if [ -n "$PID" ]; then
  echo "Found process with PID: $PID, terminating..."
  kill -9 $PID
  sleep 2
else
  echo "No running Spring application found."
fi

# 새 JAR 파일 실행
echo "Starting new Spring application..."
nohup java -jar /home/ec2-user/picket.jar --spring.profiles.active=prod > /home/ec2-user/picket.log 2>&1 &
NEW_PID=$!
sleep 5
if ps -p $NEW_PID > /dev/null; then
  echo "Application started successfully with PID: $NEW_PID"
else
  echo "Failed to start application. Check /home/ec2-user/picket.log"
  cat /home/ec2-user/picket.log
  exit 1
fi