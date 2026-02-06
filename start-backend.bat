@echo off
cd /d "%~dp0"
echo Avvio del backend Travel Planner...
call mvn clean spring-boot:run
pause
