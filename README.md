# GymFlow 健身工作室管理系统

## 📋 项目简介
基于微信小程序的现代化健身工作室管理系统，包含会员端、教练端小程序和Web管理后台。

## 🏗️ 项目结构
- `GymFlow-Backend`: Spring Boot 后端服务
- `GymFlow-Web`: Vue.js 管理后台
- `GymFlow-Coach`: 教练端微信小程序
- `GymFlow-Member`: 会员端微信小程序

## 🚀 技术栈
- **后端**: Spring Boot 2.7+, MyBatis Plus, MySQL, Redis, JWT
- **前端**: Vue 3, Element Plus, ECharts
- **小程序**: 微信小程序原生框架
- **开发工具**: IDEA, VS Code, 微信开发者工具

## 📁 快速开始
```bash
# 克隆项目
git clone <repository-url>
cd GymFlow-Workspace

# 后端启动
cd GymFlow-Backend
mvn spring-boot:run

# 前端启动
cd GymFlow-Web
npm install
npm run dev

# 小程序导入微信开发者工具
# 1. 打开微信开发者工具
# 2. 导入 GymFlow-Member 或 GymFlow-Coach
