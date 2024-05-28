java -version
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin
mkdir -p ${basePath}
mv target/backup-sql-file ${basePath}/backup-sql-file-$(uname -s)-$(uname -m).bin