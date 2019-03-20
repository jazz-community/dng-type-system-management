@echo off
rem set JAVA_HOME=C:\PROGRA~1\Java\jre1.8.0_181
rem set JAVA_HOME=C:\Program Files (x86)\Java\jre1.8.0_191

"%JAVA_HOME%\bin\java" -cp ./tsm_lib -jar tsm.jar %*
