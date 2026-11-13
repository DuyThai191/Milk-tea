@echo off
chcp 65001 > nul
title Boba Sky - He thong quan ly tra sua
echo ==============================================
echo   DANG KHOI DONG TIEM TRA SUA BOBA SKY...
echo ==============================================

cd /d "%~dp0"

:: Kiem tra neu chua compile thi compile tu dong
if not exist "bin\com\milktea\Main.class" (
    echo Dang bien dich ma nguon Java...
    if not exist "bin" mkdir bin
    javac -encoding UTF-8 -d bin src\com\milktea\models\*.java src\com\milktea\storage\*.java src\com\milktea\service\*.java src\com\milktea\util\*.java src\com\milktea\ui\*.java src\com\milktea\Main.java
)

echo Khoi chay ung dung...
start javaw -cp bin com.milktea.Main
exit
