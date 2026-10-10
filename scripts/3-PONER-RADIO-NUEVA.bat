@echo off
title Poner radio nueva (Add new station)
set /p URL=Direccion de la radio, empieza con http:// o https:// : 
set /p NOMBRE=Nombre de la radio: 
set /p DESC=Descripcion (opcional, Enter para omitir): 
set /p LOGO=Direccion del logo (opcional, Enter para omitir): 
call "%~dp0_run.bat" play "%URL%" "%NOMBRE%" "%DESC%" "%LOGO%" > "%~dp0resultado-radio.txt" 2>&1
type "%~dp0resultado-radio.txt"
echo.
echo Si la radio suena, manten apretado un boton PRESET libre durante 4 segundos.
echo Luego cambia de fuente y vuelve a ese PRESET para comprobar que quedo guardado.
pause
