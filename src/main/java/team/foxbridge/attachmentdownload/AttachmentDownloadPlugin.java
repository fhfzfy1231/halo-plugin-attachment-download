package team.foxbridge.attachmentdownload;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * Plugin main class to manage the lifecycle of the plugin.
 *
 * @author Akagi_Zen
 * @since 1.0.0
 */
@Component
public class AttachmentDownloadPlugin extends BasePlugin {

    public AttachmentDownloadPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        System.out.println("附件下载插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("附件下载插件已停止！");
    }
}
