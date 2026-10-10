@echo off
title Ver lo que suena (Now playing)
echo Pon a sonar un PRESET en la radio y luego presiona una tecla.
pause
call "%~dp0_run.bat" current > "%~dp0resultado-actual.txt" 2>&1
type "%~dp0resultado-actual.txt"
echo.
echo Resultado guardado en resultado-actual.txt
pause
