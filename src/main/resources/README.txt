To generate the side effect free method list and nondeterministic method lists:

- Clone https://github.com/t-rasmud/checker-framework and make sure it is up to date.
- Run:

export CHECKERFRAMEWORK=<whereever you checked out above>
cd $CHECKERFRAMEWORK && ./gradlew assemble

- From Randoop's root directory, run the following command.

./gradlew clean assemble
java -cp "build/libs/randoop-all-4.2.1.jar:$CHECKERFRAMEWORK/checker/dist/checker-qual.jar" randoop.resource.MethodListGen $CHECKERFRAMEWORK/checker/dist/jdk8.jar src/main/resources

Your two files will overwrite the versions in src/main/resources/.
