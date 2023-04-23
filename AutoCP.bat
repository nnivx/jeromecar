@echo off
:: =============================================================
::  if u are getting WGL error, try setting the path below.
:: =============================================================
::set java_thunk="C:\Program Files\Java\jdk1.8.0_45\bin\java.exe"
set java_thunk=java

:: =============================================================
:: DIRECTORIES
:: =============================================================
set LIB_DIR=lib
set NATIVES_DIR=native

set opts=-Xloggc:gc.log -XX:+PrintGCDetails
set CLASSPATH=%LWJGL_JAR%;%JOML_JAR%
%java_thunk% -jar -Djava.library.path=%NATIVES_DIR% %opts% AutomatedCarParking.jar %*