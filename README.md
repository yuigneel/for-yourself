<div align="center">
  <img src="https://avatars.githubusercontent.com/u/202588416?s=400&u=baab8feb9dfc2e334858b649c91045a1be6f39f5&v=4" width="200" height="200" />

  <h1>FOR-YOURSELF</h1>

  <p>
    <a href="README.md">简体中文</a> | 
    <a href="README_EN.md">English</a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/License-AGPL--3.0-blue?style=flat-square" alt="License" />
    <img src="https://img.shields.io/badge/JDK-17+-orange?style=flat-square&logo=java" alt="JDK" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.1.12-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square" alt="Build" />
    <img src="https://img.shields.io/badge/Version-0.15.13--SNAPSHOT-blue?style=flat-square" alt="Version" />
  </p>

</div>

---

## 项目介绍

### 简介

- 该项目是**FOR-YOURSELF**项目生态后端的的后台管理微服务，提供管理员认证（登录/注册）及普通用户的全生命周期管理（增删改查、封禁/解封），实现平台权限管控。

### 说明

- 关于为什么**不用Restful风格**的请求：涉及到敏感信息的一些请求（特别是账号删除等），对于Delete的请求体部分API无法适配，并且有些网站只认传统的请求方式，综合考虑放弃Restful风格。

### 关联项目

|        项目名称         |   项目概述  |        版本号        | 项目状态 |
|:-------------------:|:--------:|:-----------------:|:----:|
| **for-the-bangumi** | 动漫笔记社交网站 | 0.15.13-SNAPSHOT  | 开发中  |  

---


## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 启动步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yuigneel/for-yourself.git
cd for-yourself
```

#### 2. 一键启动中间件

双击 `docs/开发工具一键启动.bat` 即可启动开发所需的中间件

```bash
docs/开发工具一键启动.bat
```

#### 3. 启动微服务

在 IntelliJ IDEA 中依次启动以下核心模块：

1. `for-yourself-gateway`（网关服务）
2. `center-user-web`（用户中心服务）

> 其他模块可根据业务需要按需启动。


---

## 我的宠物

<p align="center"><img src="https://stone.professorlee.work/api/stone/yuigneel/for-yourself" /></p>
<p align="center"><strong>石墩子</strong></p>

---  

## 特别鸣谢

<table align="center">
  <tr>
    <td colspan="3" style="text-align:center;">
      <img src="https://img.shields.io/badge/💖-感谢-ff69b4?style=flat-square" alt="Heart" />
      <img src="https://img.shields.io/badge/🌟-支持-ffd700?style=flat-square" alt="Star" />
      <img src="https://img.shields.io/badge/🙏-感恩-4169e1?style=flat-square" alt="Pray" />
    </td>
  </tr>
  <tr>
    <th>贡献者头像</th>
    <th>贡献者昵称</th>
    <th>贡献内容</th>
  </tr>
  <tr>
    <td style="text-align:center;">
      <img src="https://avatars.githubusercontent.com/u/89632742?v=4" width="32" height="32" />
    </td>
    <td style="text-align:center;">
      <a href="https://github.com/professor-lee">professor-lee</a>
    </td>
    <td style="text-align:center;">阅读文档里面的赛博宠物石墩子一枚</td>
  </tr>
  <tr>
    <td colspan="3" style="text-align:center;">
      <strong>特别鸣谢所有为本项目做出贡献和支持的开发者们！</strong>
    </td>
  </tr>
</table>

---  

## LICENSE

### 许可证主旨

原则上，只要不违反以下原则，均可以使用：

- **保留署名** - 使用和修改代码时必须声明原作者
- **开源要求** - 无论本地部署还是作为API服务，都必须保持开源

### 许可证声明

- 本项目采用 [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html)
  开源许可证。详细条款请参阅 [AGPL-3.0 官方文档](https://www.gnu.org/licenses/agpl-3.0.en.html)。

- 本项目所有源代码均由作者独立编写，未直接复用第三方代码，故项目遵循作者本人选择的许可证发布，确保代码的开放性与可追溯性。