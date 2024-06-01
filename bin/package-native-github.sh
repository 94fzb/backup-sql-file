#!/usr/bin/env bash

java -version
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin
mkdir -p ${basePath}
binName=backup-sql-file
if [ -f "target/${binName}" ];
then
  mv target/${binName} ${basePath}/${binName}-$(uname -s)-$(uname -m).bin
fi
if [ -f "target/${binName}.exe" ];
then
  mv target/${binName}.exe ${basePath}/${binName}-Windows-$(uname -m).exe
fi