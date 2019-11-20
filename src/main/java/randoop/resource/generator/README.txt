To generate the side effect free method list and nondeterministic method lists:

- Clone https://github.com/t-rasmud/checker-framework and make sure it is up to date.
- Run:

export CHECKERFRAMEWORK=<whereever you checked out above>
cd $CHECKERFRAMEWORK && ./gradlew buildJdk -PuseLocalJdk

- Extract the jdk8.jar [un-jar] in $CHECKERFRAMEWORK/checker/jdk/ into a directory of your choosing
- From Randoop's root directory, run the following command,
    replacing the first argument with the extracted JDK:
    You can change the output directory as desired; it will output two files under your output directory.
    (omitmethods-defaults-part.txt and JDK-sef-methods.txt).

./gradlew clean assemble
java -cp build/libs/randoop-all-4.2.0.jar randoop.resource.generator.MethodListGen REPLACE-ME $CHECKERFRAMEWORK/checker/jdk/nullness/build/ $HOME/output/

Your two files will be located in your output directory.
