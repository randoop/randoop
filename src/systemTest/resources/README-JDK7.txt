To generate the methods files 'JDK7-sef-methods.txt' and 'JDK7-omitmethods.txt':

- JDK7-sef-methods.txt: Copy the generated JDK-sef-methods.txt from the output of the 
    MethodListGen tool. Replace every instance of 'java.util' within the file to 'java7.util7'.
    You may need to manually remove the methods referring to those present in JDK8 but not JDK7,
    though at the time of this writing, no such difference exists.

- JDK7-omitmethods.txt: Copy the generated JDK-nondet-methods.txt from the output of the 
    MethodListGen tool. Replace every instance of 'java.util' within the file to 'java7.util7'.
