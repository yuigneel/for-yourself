@echo off
:: 关闭命令回显，只展示关键提示
chcp 65001 >nul
:: 强制设置UTF-8编码，解决中文乱码
color 0A
:: 控制台字体设为绿色，提升可读性

echo ==============================================
echo          微服务环境一键启动（优雅版）
echo          需右键以管理员身份运行
echo ==============================================
echo.

:: ===================== 第一步：优雅停止服务 =====================
echo 【阶段1/3】尝试优雅停止已运行的服务...

:: 1. 优雅停止MySQL服务（优先系统服务停止，非暴力杀进程）
echo.
echo --- 处理MySQL服务 ---
net stop mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL服务已优雅停止
) else (
    echo ⚠️ MySQL服务未运行/停止失败，尝试检查进程...
    :: 先非强制终止MySQL进程（给进程退出时间）
    taskkill /im mysqld.exe >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ MySQL进程已非强制终止
    ) else (
        echo ⚠️ 非强制终止失败，尝试强制清理MySQL残留进程...
        taskkill /f /im mysqld.exe >nul 2>&1
        echo ✅ MySQL残留进程已强制清理
    )
)

:: 2. 优雅停止Redis服务（同MySQL逻辑）
echo.
echo --- 处理Redis服务 ---
net stop redis >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis服务已优雅停止
) else (
    echo ⚠️ Redis服务未运行/停止失败，尝试检查进程...
    taskkill /im redis-server.exe >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ Redis进程已非强制终止
    ) else (
        echo ⚠️ 非强制终止失败，尝试强制清理Redis残留进程...
        taskkill /f /im redis-server.exe >nul 2>&1
        echo ✅ Redis残留进程已强制清理
    )
)

:: 3. 优雅停止Nacos（无系统服务，优先非强制终止进程）
echo.
echo --- 处理Nacos服务 ---
tasklist | findstr /i "nacos.exe" >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 检测到Nacos进程运行，尝试非强制终止...
    taskkill /im nacos.exe >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ Nacos进程已非强制终止
    ) else (
        echo ⚠️ 非强制终止失败，尝试强制清理Nacos残留进程...
        taskkill /f /im nacos.exe >nul 2>&1
        echo ✅ Nacos残留进程已强制清理
    )
) else (
    echo ✅ Nacos进程未运行，无需停止
)

echo.
echo 【阶段1/3】优雅停止完成！
echo.

:: ===================== 第二步：最终残留兜底清理 =====================
echo 【阶段2/3】最终残留进程兜底清理...
:: 兜底清理（防止上述步骤仍有漏网的进程）
taskkill /f /im mysqld.exe >nul 2>&1
taskkill /f /im redis-server.exe >nul 2>&1
taskkill /f /im nacos.exe >nul 2>&1
echo ✅ 残留进程兜底清理完成！
echo.

:: ===================== 第三步：启动所有服务 =====================
echo 【阶段3/3】开始启动服务...

:: 启动MySQL服务
echo.
echo --- 启动MySQL服务 ---
net start mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL服务启动成功
) else (
    echo ❌ MySQL服务启动失败，请检查服务是否存在/权限是否足够
)

:: 启动Redis服务
echo.
echo --- 启动Redis服务 ---
net start redis >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis服务启动成功
) else (
    echo ❌ Redis服务启动失败，请检查服务是否存在/权限是否足够
)

:: 启动Nacos（单机开发模式）
echo.
echo --- 启动Nacos单机模式 ---
set "nacos_bin_path=D:\Yu_Lgnier\Development_System\File_Roots\nacos\bin"
if exist "%nacos_bin_path%\startup.cmd" (
    cd /d "%nacos_bin_path%"
    :: 新开窗口启动Nacos，不占用当前脚本窗口
    start cmd /k "startup.cmd -m standalone"
    echo ✅ Nacos启动命令已执行（新窗口运行）
) else (
    echo ❌ Nacos路径不存在：%nacos_bin_path%
)

echo.
echo ==============================================
echo ✅ 所有服务启动流程执行完毕（请检查新窗口Nacos是否正常）
echo ==============================================
pause
:: 暂停窗口，防止执行完自动关闭