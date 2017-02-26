set TESTED_CLASS=java.util.ArrayDeque
set TOOLS_FOLDER=C:\randoop\dist\bin
set MAX_SIZE=2
set /a PREVIOUS_SIZE="%MAX_SIZE%-1"
set EXEC_CLASSPATH="%TOOLS_FOLDER%\guava-19.0.jar;%TOOLS_FOLDER%\plume.jar;%TOOLS_FOLDER%\exercised-class-3.0.8.jar;%TOOLS_FOLDER%\randoop-3.0.8.jar"
set BASE_TESTS_OUTPUT_FOLDER="tests_output"
SET TESTS_OUTPUT_FOLDER="%BASE_TESTS_OUTPUT_FOLDER%/%MAX_SIZE%"
SET LITERALS_FILE="literals_randoop.txt"
SET TESTS_PER_FILE=500
SET LOG_FILE="log\%TESTED_CLASS%_size_%MAX_SIZE%_log.txt"
SET PREVIOUS_SIZE_INDEX_FILE="%BASE_TESTS_OUTPUT_FOLDER%/%PREVIOUS_SIZE%/current_seq_index.tar.gz"

IF "%MAX_SIZE%" == "2" ( 
	java -cp "%EXEC_CLASSPATH%" randoop.main.Main gentests --outputlimit=2147483647 --inputlimit=2147483647 --timelimit=604800  --checked-exception=INVALID --unchecked-exception=INVALID --npe-on-null-input=INVALID --oom-exception=INVALID "--omitmethods=\.(toString|hashCode|util\.function|util\.stream)" --usethreads=true --only-test-public-members=true --maxsize=%MAX_SIZE% --junit-output-dir=%TESTS_OUTPUT_FOLDER% --ignore-flaky-tests=true --literals-level=ALL --literals-file=%LITERALS_FILE% --testsperfile=%TESTS_PER_FILE% --log=%LOG_FILE% --testclass=%TESTED_CLASS%
	) ELSE (
			java -cp "%EXEC_CLASSPATH%" randoop.main.Main gentests --outputlimit=2147483647 --inputlimit=2147483647 --timelimit=604800  --checked-exception=INVALID --unchecked-exception=INVALID --npe-on-null-input=INVALID --oom-exception=INVALID "--omitmethods=\.(toString|hashCode|util\.function|util\.stream)" --usethreads=true --only-test-public-members=true --maxsize=%MAX_SIZE% --junit-output-dir=%TESTS_OUTPUT_FOLDER% --ignore-flaky-tests=true --literals-level=ALL --literals-file=%LITERALS_FILE% --testsperfile=%TESTS_PER_FILE% --log=%LOG_FILE% --testclass=%TESTED_CLASS% --generation-index-file=%PREVIOUS_SIZE_INDEX_FILE%
		   )

%TOOLS_FOLDER%\SwithMail.exe /s /x "%TOOLS_FOLDER%\SwithMailSettings.xml"
