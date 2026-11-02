# Halo 附件下载插件 (Attachment Download)

为 [Halo](https://halo.run/) 默认富文本编辑器提供受权限保护的附件下载功能。

## ✨ 特性

- **编辑器集成**：在 Halo 默认编辑器中一键从附件库选取文件并插入下载卡片。
- **RBAC 权限保护**：支持基于 Halo 权限中心进行细粒度权限控制，允许为指定角色/用户组（如“网站访客用户组”）分配“下载受保护附件”权限。
- **直链隐藏与防盗链**：不在前台暴露附件真实静态 URL，所有请求通过受控端点代理下载。
- **强制下载**：对 XML、图片、文本等浏览器默认预览的文件强制附加 `Content-Disposition: attachment` 下载头。
- **前台友好拦截**：未获授权或未登录用户点击时弹窗友好提示，保障附件资源安全。

## 📦 构建与发布

### 本地构建
```bash
# 1. 编译前端
cd ui
pnpm install
pnpm build
cd ..

# 2. 编译插件 Jar
./gradlew build -x test
```
生成产物位于 `build/libs/plugin-attachment-download-*.jar`。

### GitHub Actions 自动构建
本项目已配置 GitHub Actions 自动构建工作流（`.github/workflows/release.yaml`）。
只需在 GitHub 创建 Release 或推送 `v*` 格式标签（如 `v1.1.1`），即可全自动编译并将 `.jar` 文件附加到 Release 资源列表中供下载。

## 📄 开源许可
[GPL-3.0 License](LICENSE)
