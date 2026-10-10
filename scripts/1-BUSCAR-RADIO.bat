@echo off
title Buscar radio (Find radio)
echo Buscando la radio en la red, espera unos 15 segundos...
call "%~dp0_run.bat" list > "%~dp0resultado-buscar.txt" 2>&1
type "%~dp0resultado-buscar.txt"
echo.
echo Resultado guardado en resultado-buscar.txt
pause
