To generate the side effect free method list and nondeterministic method lists:

- Clone https://github.com/t-rasmud/checker-framework and make sure it is up to date.
- Run:

export CHECKERFRAMEWORK=<whereever you checked out above>
cd $CHECKERFRAMEWORK && ./gradlew assemble
cd $CHECKERFRAMEWORK/checker/dist && rm -rf jdk8-jar && unzip -q jdk8.jar -d jdk8-jar

- From Randoop's root directory, run the following command.

./gradlew clean assemble
java -cp build/libs/randoop-all-4.2.0.jar randoop.resource.generator.MethodListGen $CHECKERFRAMEWORK/checker/dist/jdk8-jar src/main/resources

Your two files will overwrite the versions in src/main/resources/.
