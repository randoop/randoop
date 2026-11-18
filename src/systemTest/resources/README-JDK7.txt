To generate the methods files 'JDK7-sef-methods.txt' and 'JDK7-omitmethods.txt':

- JDK7-sef-methods.txt: Copy the generated JDK-sef-methods.txt from the output of the 
    MethodListGen tool. Replace every instance of 'java.util' within the file to 'java7.util7'.
    You may need to manually remove the methods referring to those present in JDK8 but not JDK7.

    You can generate the list of methods to exclude by adding logging to failed method parsing to the throw RandoopUsageError block in
    OperationModel.readOperation(EntryReader, boolean) and making it continue rather than throwing an exception.
    At the time of this writing, the list of methods removed are in JDK7-sef-subtract.txt. 
    You can subtract lines of one file from another by 
    'grep -F -x -v -f subtract.txt all.txt > result.txt'.

- JDK7-omitmethods.txt: Copy the generated JDK-nondet-methods.txt from the output of the 
    MethodListGen tool. Replace every instance of 'java.util' within the file to 'java7.util7'.
