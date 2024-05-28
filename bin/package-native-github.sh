java -version
./mvnw clean package
./mvnw -Pnative -Dagent exec:exec@java-agent -U
./mvnw -Pnative package
basePath=/tmp/download/plugin
mkdir -p ${basePath}
mv target/buckup-sql-file ${basePath}/buckup-sql-file-$(uname -s)-$(uname -m).bin