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

## Project Introduction

### Introduction

- This project is the backend administration microservice for the **FOR-YOURSELF** project ecosystem. It provides administrator authentication (login/registration) and full lifecycle management for regular users (CRUD, ban/unban), implementing platform permission control.

### Note

- Regarding why **RESTful style** requests are not used: For requests involving sensitive information (especially account deletion, etc.), the Delete request body is not adaptable for some APIs, and some websites only recognize traditional request methods. After comprehensive consideration, the RESTful style was abandoned.

### Related Projects

|        Project Name         |   Project Overview  |        Version        | Project Status |
|:-------------------:|:--------:|:-----------------:|:----:|
| **for-the-bangumi** | Anime Note Social Website | 0.15.13-SNAPSHOT  | In Development  |  

---

## Quick Start

### Environment Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### Startup Steps

#### 1. Clone the Project

```bash
git clone https://github.com/yuigneel/for-yourself.git
cd for-yourself
```

#### 2. Start Middleware

Double-click `docs/开发工具一键启动.bat` to start the middleware required for development

```bash
docs/开发工具一键启动.bat
```

#### 3. Start Microservices

Start the following core modules in IntelliJ IDEA one by one:

1. `for-yourself-gateway` (Gateway Service)
2. `center-user-web` (User Center Service)

> Other modules can be started according to business needs.


---

## My Pet

<p align="center"><img src="https://stone.professorlee.work/api/stone/yuigneel/for-yourself" /></p>
<p align="center"><strong>Stone Pedestal</strong></p>

---  

## Special Thanks

<table align="center">
  <tr>
    <td colspan="3" style="text-align:center;">
      <img src="https://img.shields.io/badge/💖-Thanks-ff69b4?style=flat-square" alt="Heart" />
      <img src="https://img.shields.io/badge/🌟-Support-ffd700?style=flat-square" alt="Star" />
      <img src="https://img.shields.io/badge/🙏-Gratitude-4169e1?style=flat-square" alt="Pray" />
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
    <td style="text-align:center;">Read the documentation for a cyber pet stone pedestal</td>
  </tr>
  <tr>
    <td colspan="3" style="text-align:center;">
      <strong>Special thanks to all developers who have contributed to and supported this project!</strong>
    </td>
  </tr>
</table>

---  

## LICENSE

### License Purpose

In principle, as long as the following principles are not violated, they can be used:

- **Attribution** - Must declare the original author when using and modifying the code
- **Open Source Requirements** - Whether deployed locally or used as an API service, it must remain open source

### License Declaration

- This project uses the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.html)
  open-source license. Detailed terms please refer to the [AGPL-3.0 official document](https://www.gnu.org/licenses/agpl-3.0.en.html).

- All source code in this project was independently written by the author and does not directly reuse third-party code. Therefore, the project is released under the license chosen by the author, ensuring the openness and traceability of the code.