@echo off
set MAX_SIZE=5
set TESTED_CLASS=java.net.DatagramSocket
set TOOLS_FOLDER=C:\randoop\dist\bin
set /a PREVIOUS_SIZE="%MAX_SIZE%-1"
set EXEC_CLASSPATH="%TOOLS_FOLDER%\guava-19.0.jar;%TOOLS_FOLDER%\plume.jar;%TOOLS_FOLDER%\exercised-class-3.0.8.jar;%TOOLS_FOLDER%\randoop-3.0.8.jar"
set BASE_TESTS_OUTPUT_FOLDER="tests_output"
SET TESTS_OUTPUT_FOLDER="%BASE_TESTS_OUTPUT_FOLDER%/%MAX_SIZE%"
SET LITERALS_FILE="literals_randoop.txt"
SET TESTS_PER_FILE=100
SET LOG_FILE="log\%TESTED_CLASS%_size_%MAX_SIZE%_log.txt"
SET CURRENT_SIZE_INDEX_FILE="%BASE_TESTS_OUTPUT_FOLDER%/%MAX_SIZE%/current_seq_index.tar.gz"
SET PREVIOUS_SIZE_INDEX_FILE="%BASE_TESTS_OUTPUT_FOLDER%/%PREVIOUS_SIZE%/current_seq_index.tar.gz"
SET DEFAULT_OPTIONS= --outputlimit=2147483647 --inputlimit=2147483647 --timelimit=604800  --checked-exception=INVALID --unchecked-exception=INVALID --npe-on-null-input=INVALID --oom-exception=INVALID --output_only_sequences=true --usethreads=true --only-test-public-members=true --ignore-flaky-tests=true --literals-level=ALL "--omitmethods=\.(toString|hashCode|util\.function|util\.stream)"
SET RANDOOP_ARGS=-cp %EXEC_CLASSPATH% randoop.main.Main gentests %DEFAULT_OPTIONS% --maxsize=%MAX_SIZE% --junit-output-dir=%TESTS_OUTPUT_FOLDER% --literals-file=%LITERALS_FILE% --testsperfile=%TESTS_PER_FILE% --log=%LOG_FILE% --testclass=%TESTED_CLASS% 


REM ASSUMING THE GENERATION STARTS WITH SIZE 2, AND GENERATION OF SIZE N ALWAYS CONTINUES FROM SIZE N-1
@echo on

IF "%MAX_SIZE%" == "2" ( 
	IF exist CURRENT_SIZE_INDEX_FILE (
		ECHO Resuming generation of sequences of size 2 for class %TESTED_CLASS% starting with index file
		java %RANDOOP_ARGS% --generation-index-file=%CURRENT_SIZE_INDEX_FILE%
	) ELSE (
				ECHO Starting the generation of sequences of size 2 for class %TESTED_CLASS% starting with no index file
				java %RANDOOP_ARGS%
			)
	) ELSE (
			IF exist CURRENT_SIZE_INDEX_FILE (
				ECHO Resuming generation of sequences of size %MAX_SIZE% for class %TESTED_CLASS% starting with index file of previous execution of same length
				java %RANDOOP_ARGS% --generation-index-file=%CURRENT_SIZE_INDEX_FILE%
			) ELSE (		
					ECHO Resuming generation of sequences of size %MAX_SIZE% for class %TESTED_CLASS% starting with index file of previous execution of length %PREVIOUS_SIZE%
					 java %RANDOOP_ARGS% --generation-index-file=%PREVIOUS_SIZE_INDEX_FILE%
			       )		
		   )


%TOOLS_FOLDER%\SwithMail.exe /s /x "%TOOLS_FOLDER%\SwithMailSettings.xml"

