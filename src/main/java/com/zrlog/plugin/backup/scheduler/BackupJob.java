package com.zrlog.plugin.backup.scheduler;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.backup.Start;
import com.zrlog.plugin.backup.scheduler.handle.BackupExecution;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.SecurityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackupJob implements Runnable {

    private static final Logger LOGGER = LoggerUtil.getLogger(BackupJob.class);

    private final IOSession ioSession;
    private final String propFile;

    public BackupJob(IOSession ioSession, String propFile) {
        this.ioSession = ioSession;
        this.propFile = propFile;
    }

    public static File backupThenStoreToPrivateStore(IOSession ioSession, String propertiesFile) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            URI uri = new URI(properties.getProperty("jdbcUrl").replace("jdbc:", ""));
            String dbName = uri.getPath().replace("/", "");
            StringJoiner sj = new StringJoiner("_");
            sj.add(dbName);
            sj.add(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            sj.add(UUID.randomUUID().toString().replace("-", ""));
            File dbFile = new File(Start.sqlPath + sj + ".sql");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            BackupExecution backupExecution = new BackupExecution();
            File tempFile = backupExecution.dumpToFile(properties.getProperty("user"), uri.getPort(),
                    uri.getHost(), dbName, properties.getProperty("password"));
            String newFileMd5 = SecurityUtils.md5ByFile(tempFile);
            for (File file : dbFile.getParentFile().listFiles()) {
                if (Objects.equals(newFileMd5, SecurityUtils.md5ByFile(file))) {
                    tempFile.delete();
                    return file;
                }
            }
            tempFile.renameTo(dbFile);
            try {
                Map<String, String[]> map = new HashMap<>();
                map.put("fileInfo", new String[]{dbFile + "," + dbName + "/" + dbFile.getName()});
                ioSession.requestService("uploadToPrivateService", map);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "UploadToPrivate error", e);
            }
            return dbFile;
        }

    }

    public static void clearFile() {
        File dbPath = new File(Start.sqlPath);
        if (dbPath.exists()) {
            File[] files = dbPath.listFiles();
            if (files != null) {
                List<File> fileList = new ArrayList<>();
                for (File file : files) {
                    if (file.getName().endsWith(".sql")) {
                        fileList.add(file);
                    }
                }
                if (fileList.size() > Start.maxBackupSqlFileCount) {
                    fileList.sort(Comparator.comparingLong(File::lastModified));
                    List<File> needRemoveFileList = fileList.subList(0, fileList.size() - Start.maxBackupSqlFileCount);
                    for (File file : needRemoveFileList) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            backupThenStoreToPrivateStore(ioSession, propFile);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "jdbcUrl error", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        } finally {
            clearFile();
        }
    }
}
