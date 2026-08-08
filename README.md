[![Halo](https://img.shields.io/badge/Halo-%3E%3D2.25.0-1784C7?labelColor=3C3C3C)](https://www.halo.run/)
[![License](https://img.shields.io/badge/License-GPL--3.0-78B927?labelColor=3C3C3C)](./LICENSE)

作者：[Akagi_Zen](https://github.com/fhfzfy1231/halo-plugin-attachment-download)

# Halo 附件下载

一个为 Halo 默认富文本编辑器提供附件下载能力的插件。

发布文章时，可以直接从 Halo 附件库选择文件并插入下载块。访客点击下载按钮后，插件会通过 Halo 权限系统检查下载权限，并以附件形式返回文件，避免 XML、TXT 等浏览器可直接预览的文件在新页面中打开。

特别适合发布 **Juno: New Origins / SimpleRockets 2** 的飞行器 XML 文件、配置文件、压缩包以及其他需要提供下载的附件。

## 功能特点

- 在 Halo 默认编辑器的 `+` 工具箱中增加“附件下载”。
- 支持输入 `/` 搜索“附件下载”并快速插入。
- 直接调用 Halo 自带附件库选择文件，无需重复上传。
- 一个下载块对应一个附件，并支持随时重新选择。
- 自动显示并保存附件名称、文件大小和 Halo 附件标识。
- 提供独立的“下载受保护附件”Halo 角色权限。
- 只有被授予下载权限的用户才能获取附件。
- 未登录或没有下载权限时，点击下载按钮会提示“请登录后下载”。
- 下载请求经过插件后端进行 RBAC 权限校验，不能只通过绕过前端按钮直接下载。
- 使用 `Content-Disposition: attachment` 强制浏览器下载附件，避免 XML 等文件被直接打开。
- 新版下载块不会在文章 HTML 中暴露附件原始 URL。
- 下载块样式独立，不依赖当前 Halo 主题的 CSS。

## 环境要求

- Halo >= 2.25.0
- Halo 默认富文本编辑器

## 安装

1. 在本仓库的 [Releases](https://github.com/fhfzfy1231/halo-plugin-attachment-download/releases) 页面下载最新版本的 JAR 文件。
2. 登录 Halo 管理后台。
3. 进入“插件”页面，点击“安装插件”。
4. 上传下载的 JAR 文件并完成安装。
5. 启用“附件下载”插件。

## 使用方法

1. 进入 Halo 文章编辑页面。
2. 点击编辑器中的 `+`，选择“附件下载”；也可以输入 `/` 搜索“附件下载”。
3. 点击“选择附件”，从 Halo 附件库选择需要提供下载的文件。
4. 确认下载块中显示的附件名称和大小后发布文章。
5. 拥有下载权限的用户登录后，点击“下载文件”即可直接保存附件。

## 下载权限

插件提供以下 Halo 角色权限：

```text
附件下载
└─ 下载受保护附件
```

需要允许普通用户下载附件时：

1. 在 Halo 后台创建或编辑角色。
2. 在角色权限中找到“附件下载”。
3. 勾选“下载受保护附件”。
4. 将该角色分配给允许下载的用户。

超级管理员拥有下载权限。普通用户只有在获得相应角色权限后才能下载；未登录用户和无权限用户点击按钮时都会收到“请登录后下载”的提示。

## 从 v1.0.x 升级

`v1.0.x` 中已经插入文章的旧下载块只保存了旧版附件信息。升级到 `v1.1.0` 后，请进入对应文章的编辑器，对旧下载块执行一次“重新选择”，并重新发布文章，使其使用新的受保护附件标识和权限下载链路。

## 开发与构建

克隆项目：

```bash
git clone https://github.com/fhfzfy1231/halo-plugin-attachment-download.git
cd halo-plugin-attachment-download
```

构建插件：

```bash
./gradlew clean build
```

构建完成后的插件位于：

```text
build/libs/
```

## 相关链接

- [项目主页](https://github.com/fhfzfy1231/halo-plugin-attachment-download)
- [Releases](https://github.com/fhfzfy1231/halo-plugin-attachment-download/releases)
- [问题反馈](https://github.com/fhfzfy1231/halo-plugin-attachment-download/issues)
- [Halo](https://www.halo.run/)

## 许可证

本项目基于 [GPL-3.0](./LICENSE) 开源。

