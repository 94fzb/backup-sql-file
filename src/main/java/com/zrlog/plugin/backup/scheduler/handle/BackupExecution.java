package com.zrlog.plugin.backup.scheduler.handle;

import com.zrlog.plugin.RunConstants;
import com.zrlog.plugin.common.IOUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.PathKit;
import com.zrlog.plugin.type.RunType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackupExecution {

    private static final Logger LOGGER = LoggerUtil.getLogger(BackupExecution.class);

    public static void main(String[] args) throws IOException {
        System.out.println(getBinFile());
    }

    private static File getBinFile() throws IOException {
        File binFile;
        if (testMysqlDumpInstalled()) {
            binFile = new File("mysqldump");
        } else {
            String path = System.getProperties().getProperty("os.arch") + "/" + System.getProperties().getProperty(
                    "os.name").toLowerCase().replace(" ", "") + "/mysqldump";
            binFile = new File(PathKit.getTmpPath() + "/" + path);
            LOGGER.info("Temp file " + binFile + ", path " + path);
            copyInternalFileTo(BackupExecution.class.getResourceAsStream("/lib/" + path), binFile);
            //unix 设置执行权限
            if ("/".equals(File.separator)) {
                Runtime.getRuntime().exec("chmod 777 " + binFile);
            }
        }
        return binFile;
    }

    /**
     * 系统内是否安装了 mysqlDump
     */
    private static boolean testMysqlDumpInstalled() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mysqldump");
            process.destroy();
            return true;
        } catch (IOException e) {
            if (RunConstants.runType == RunType.DEV) {
                LOGGER.log(Level.SEVERE, "UnSupport mysqldump", e);
            }
            return false;
        }
    }

    private static void copyInternalFileTo(InputStream inputStream, File file) {
        if (inputStream == null) {
            return;
        }
        byte[] tempByte = new byte[1024];
        try {
            int length;
            file.getParentFile().mkdirs();
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                while ((length = inputStream.read(tempByte)) != -1) {
                    fileOutputStream.write(tempByte, 0, length);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "stream error", e);
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "stream error", e);
            }
        }
    }

    public byte[] getDumpFileBytes(String user, int port, String host, String dbName, String password) throws Exception {
        if (RunConstants.runType == RunType.DEV) {
            LOGGER.info("DumpFile start");
        }

        String execString = getBinFile() + " -f -h" + host + " -P" + port + "  -u" + user + " -p" + password + " " +
                "--databases " + dbName;
        if (RunConstants.runType == RunType.DEV) {
            LOGGER.info(execString);
        }
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(execString);
        String[] strArr = IOUtil.getStringInputStream(process.getErrorStream()).split("\r\n");
        if (strArr.length == 0) {
            LOGGER.log(Level.SEVERE, "The system not support mysqldump cmd \n");
            return new byte[0];
        }
        process.destroy();
        StringJoiner sj = new StringJoiner("\r\n");
        for (String sql : strArr) {
            //ignore time info
            if (sql.startsWith("-- Dump completed on")) {
                continue;
            }
            sj.add(sql);
        }
        return sj.toString().getBytes();
    }
}
