@echo off
REM Starts the whole platform, each part in its own window.
REM Prerequisite: PostgreSQL running on localhost:5432 with databases product_db and auth_db.
cd /d "%~dp0"

start "discovery-server :8761" cmd /k mvnw.cmd -pl discovery-server spring-boot:run
echo Waiting 20 seconds for the discovery server...
timeout /t 20 /nobreak >nul

start "auth-service :8082"    cmd /k mvnw.cmd -pl auth-service spring-boot:run
start "product-service :8081" cmd /k mvnw.cmd -pl product-service spring-boot:run
start "api-gateway :8090"     cmd /k mvnw.cmd -pl api-gateway spring-boot:run
start "frontend :4200"        cmd /k "cd frontend && npm start"

echo.
echo All parts are starting in separate windows.
echo Open http://localhost:4200 in about 30 seconds.
echo To stop everything, close those windows (or press Ctrl+C in each).
