@echo off

set argc=0
FOR %%x in (%*) DO (
    set /A argc+=1
    set "argv[!argc!]=%%~x"
)

IF /I "%argc%" NEQ "1" (
    echo "Exacly one argument required, the name of the main class" >&2
) ELSE (
    gradlew exec -PmainClass=%1
)
