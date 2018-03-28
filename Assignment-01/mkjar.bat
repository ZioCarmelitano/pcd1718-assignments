@echo off

set BENCHMARK_FLAG="-b"
set GUI_FLAG="-g"

set argc=0
FOR %%x in (%*) DO (
    set /A argc+=1
    set "argv[!argc!]=%%~x"
)

IF /I "%argc%" NEQ "1" (
    echo Usage: %0 <-b|-g> >&2
    echo     %0 %BENCHMARK_FLAG% run benchmark >&2
    echo     %0 %GUI_FLAG% run GUI >&2
) ELSE IF /I "%1" == %BENCHMARK_FLAG% (
    gradlew mkBenchmarkJar
) ELSE IF /I "%1" == %GUI_FLAG% (
    gradlew mkGuiJar
) ELSE (
    echo Usage: %0 <-b|-g> >&2
    echo     %0 %BENCHMARK_FLAG% run benchmark >&2
    echo     %0 %GUI_FLAG% run GUI >&2
)
