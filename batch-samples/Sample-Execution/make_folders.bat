@echo off
IF [%1] == [] GOTO fail_no_package_name
IF [%2] == [] GOTO fail_no_classes_files


set CLASSES_FILES=%2

IF NOT EXIST "%CLASSES_FILES%" goto fail_class_file_inexistent


IF NOT EXIST "%CLASSES_FILES%" goto fail_class_file_inexistent

set PACKAGE_NAME=%1
set EXAMPLES_FOLDER="Example v2 - use parent class name as test class"

IF NOT EXIST %EXAMPLES_FOLDER% goto fail_no_examples_folder

Echo Creating folder %PACKAGE_NAME% if not existent
if NOT exist %PACKAGE_NAME% mkdir %PACKAGE_NAME%

FOR /F %%G IN (%CLASSES_FILES%) DO (
	ECHO Creating folder %PACKAGE_NAME%\%%G

	IF NOT EXIST "%PACKAGE_NAME%\%%G" mkdir "%PACKAGE_NAME%\%%G"

	robocopy %EXAMPLES_FOLDER% "%PACKAGE_NAME%\%%G" /E
)

goto exit

:fail_no_package_name
	ECHO Package name not informed. Run the batch informing it as the first parameter.
	goto exit

:fail_no_classes_files
	ECHO File containing one class per line not informed. Run the batch informing it as the second parameter.
	goto exit

:fail_class_file_inexistent
	ECHO Could not find file containing classes. Checking the spelling and access to file %CLASSES_FILES%
	goto exit

:fail_no_examples_folder
	ECHO Could not find folder containing examples. Check the variable set in %EXAMPLES_FOLDER%
	goto exit	

:exit	