changequote dnl
changequote(`[',`]')dnl
changecom([], [Disable comments, that is, expand within them.])dnl
ifelse([The built-in "dnl" macro means "discard to next line".,])dnl
define([canary_version], [24])dnl
ifelse([each macro takes two arguments, the OS name and the JDK version])dnl
dnl
define([circleci_boilerplate_pre], [dnl
    resource_class: large

    steps:

      - restore_cache:
          keys:
            - source-v2$1-{{ .Branch }}-{{ .Revision }}
            - source-v2$1-{{ .Branch }}-
            - source-v2$1-
      - checkout[]ifelse($1,full,[:
          method: full])
      - save_cache:
          key: source-v2$1-{{ .Branch }}-{{ .Revision }}
          paths:
            - ".git"

      - restore_cache:
          keys:
            - gradle-v2-{{ .Branch }}-{{ checksum "build.gradle" }}
            - gradle-v2-{{ .Branch }}-
            - gradle-v2-
])dnl
dnl
define([circleci_boilerplate_post], [
      - save_cache:
          paths:
            - ~/.gradle
          key: gradle-v2-{{ .Branch }}-{{ checksum "build.gradle" }}
])dnl
dnl
define([circleci_test_results], [dnl
      # From https://circleci.com/docs/2.0/collect-test-data/#gradle-junit-results
      - run:
          name: Save test results
          command: |
            mkdir -p ~/test-results/junit/
            find . -type f -regex ".*/build/test-results/.*xml" -exec cp {} ~/test-results/junit/ \;
          when: always
      - store_test_results:
          path: ~/test-results
      - store_artifacts:
          path: ~/test-results/junit])dnl
dnl
define([nonSystemTest_job], [dnl
  nonSystemTest-jdk$1:
    docker:
      - image: mdernst/randoop-ubuntu-jdk$1
circleci_boilerplate_pre
      - run: ./scripts/test-nonSystemTest.sh
circleci_boilerplate_post
circleci_test_results
])dnl
dnl
define([systemTest_job], [dnl
  systemTest-jdk$1:
    docker:
      - image: mdernst/randoop-ubuntu-jdk$1
circleci_boilerplate_pre
      - run:
          command: ./scripts/test-systemTest.sh
          no_output_timeout: 20m
circleci_boilerplate_post
circleci_test_results
])dnl
dnl
define([misc_job], [dnl
  misc:
    docker:
      - image: mdernst/randoop-ubuntu-jdkany
circleci_boilerplate_pre(full)
      - run: ./scripts/test-misc.sh
circleci_boilerplate_post])dnl
dnl
dnl
ifelse([Example arguments: (8, systemTest)],,)dnl
define([job_dependences], [dnl
      - $2-jdk$1[]dnl
ifelse($1,canary_version,,[:
          requires:
            - misc
            - $2-jdk[]canary_version[]])dnl
])dnl
dnl
ifelse([
Local Variables:
eval: (add-hook 'after-save-hook '(lambda () (run-command nil "make")) nil 'local)
end:
])dnl
