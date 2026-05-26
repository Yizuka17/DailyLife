# 日簿记

<p align="center">
    <a href="https://developer.android.com">
        <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform Android"/>
    </a>
    <a href="https://kotlinlang.org">
        <img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.1"/>
    </a>
    <a href="https://developer.android.com/jetpack/compose">
        <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
    </a>
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/License-CNC--1.0-6B7280" alt="License CNC-1.0"/>
    </a>
</p>

> 语言： [English](README.md) | 简体中文

日簿记是一款基于 Jetpack Compose 的个人记账应用，用于记录日常收支、查看统计、管理账户，并支持本地数据备份与恢复。

当前维护仓库： https://github.com/Yizuka17/DailyLife

原始上游仓库： https://github.com/Evening-01/DailyLife

## 功能

- 收支记录：支持新增、编辑、分类、备注、心情、账户绑定与软删除。
- 数据统计：月度概览、明细列表、图表、分类排行、心情趋势。
- 资产账户：支持账户余额、账户类型、默认账户与排序。
- 自定义分类：支持支出/收入分类管理。
- 个性化：主题、动态取色、字体缩放、自定义字体、语言、昵称、签名、头像。
- 数据管理：备份/恢复交易、分类、账户、偏好设置、提醒设置与头像图片数据。
- 工具：房贷计算器、汇率换算。

## 技术栈

- Kotlin 2.1
- Jetpack Compose + Material 3
- Hilt
- Room
- Coroutines + Flow
- FastKV
- AndroidX Biometric
- Min SDK 26 / Target SDK 35

## 项目结构

```text
app/src/main/java/com/yizuka17/dailylife/
├── app/        # 应用入口、主界面、导航
├── core/       # 数据库、仓库、偏好设置、DI、设计系统、工具类
└── feature/    # 功能模块：home、transaction、chart、assets、me 等
```

## 构建

1. 克隆仓库：

```bash
 git clone https://github.com/Yizuka17/DailyLife.git
 cd DailyLife
```

2. 使用 Android Studio 打开，或通过命令行构建：

```bash
 ./gradlew assembleDebug
```

3. 可选发布签名：
   - 复制 `keystore.properties.example` 为 `keystore.properties`。
   - 填写你的签名配置。

## 常用命令

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
./gradlew clean
```

## 截图

截图可以放在 [`images`](images/) 目录中。

## 许可证

本项目采用 [Cooperative Non-Commercial License v1.0](LICENSE) 授权。
