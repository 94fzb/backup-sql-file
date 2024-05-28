package com.zrlog.plugin.backup;

import com.google.gson.Gson;
import com.zrlog.plugin.backup.controller.BackupController;
import com.zrlog.plugin.common.PluginNativeImageUtils;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.FreeMarkerRenderHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        new Gson().toJson(new HttpRequestInfo());
        new Gson().toJson(new Plugin());
        new Gson().toJson(new HashMap<>());
        new Gson().fromJson("{}", Map.class);
        String basePath = System.getProperty("user.dir").replace("/target", "");
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath() + "/", "/");
        //Application.nativeAgent = true;
        new FreeMarkerRenderHandler().render("/templates/index.ftl", new Plugin(), new HashMap<>());
        PluginNativeImageUtils.exposeController(Collections.singletonList(BackupController.class));
        Application.main(args);

    }
}