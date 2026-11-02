@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "changed=0"
for /f "delims=" %%F in ('git diff --name-only HEAD ^& git ls-files --others --exclude-standard') do call :detect "%%F"

if "!changed!"=="0" (
  echo NO CHANGES DETECTED
  exit /b 0
)

if defined auth (
  call :compile_auth
  if errorlevel 1 exit /b 1
)

if defined api (
  call :compile_api
  if errorlevel 1 exit /b 1
)

if defined e2e (
  call :compile_e2e
  if errorlevel 1 exit /b 1
)

if defined frontend (
  call :compile_frontend
  if errorlevel 1 exit /b 1
)

echo CHECK OK: !modules!
exit /b 0

:compile_auth
call gradlew.bat :rfr-auth:compileJava -q
if errorlevel 1 exit /b 1
call :append rfr-auth
exit /b 0

:compile_api
call gradlew.bat :rfr-api:compileJava -q
if errorlevel 1 exit /b 1
call :append rfr-api
exit /b 0

:compile_e2e
call gradlew.bat :rfr-e2e:compileTestJava -q
if errorlevel 1 exit /b 1
call :append rfr-e2e
exit /b 0

:compile_frontend
pushd rfr-gql-client
call npx tsc --noEmit
if errorlevel 1 goto frontend_failed
popd
call :append frontend
exit /b 0

:frontend_failed
popd
exit /b 1

:detect
set "file=%~1"
if /i "%~x1"==".bak" exit /b 0
echo(!file!| findstr /b /c:"rfr-auth/" >nul
if not errorlevel 1 (
  set "auth=1"
  set "changed=1"
)
echo(!file!| findstr /b /c:"rfr-api/" >nul
if not errorlevel 1 (
  set "api=1"
  set "changed=1"
)
echo(!file!| findstr /b /c:"rfr-e2e/" >nul
if not errorlevel 1 (
  set "e2e=1"
  set "changed=1"
)
echo(!file!| findstr /b /c:"rfr-gql-client/src/" >nul
if not errorlevel 1 (
  set "frontend=1"
  set "changed=1"
)
exit /b 0

:append
if defined modules (
  set "modules=!modules!, %~1"
) else (
  set "modules=%~1"
)
exit /b 0
