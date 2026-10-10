@echo off
cd /d "%~dp0"
"%~dp0jre\bin\java.exe" -Djava.library.path="%~dp0lib" -cp "%~dp0AllPlayTool.jar;%~dp0tchaikovsky.jar" AllPlayTool %*
