@rem
@rem Gradle startup script for Windows
@rem
@rem If gradle\wrapper\gradle-wrapper.jar is missing (binary file, not part of
@rem this text-only export), open the project in Android Studio once to let
@rem it regenerate the wrapper, or run "gradle wrapper" with a local install.
@rem

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

if "%JAVA_HOME%"=="" (
    set JAVA_EXE=java.exe
) else (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" -Dorg.gradle.appname=%~n0 -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
