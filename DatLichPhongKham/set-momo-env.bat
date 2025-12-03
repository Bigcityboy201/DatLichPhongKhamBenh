@echo off
REM Script to set MoMo environment variables for Windows
REM Run this before starting the Spring Boot application

set MOMO_PARTNER_CODE=MOMOD3WW20251129_TEST
set MOMO_ACCESS_KEY=WYyd3BeN9LE4S0W1
set MOMO_SECRET_KEY=OdYnJGOsh3s3ArEfMmKh5L81BvxY29Kk

echo MoMo environment variables set!
echo Starting Spring Boot application...
call mvnw spring-boot:run


