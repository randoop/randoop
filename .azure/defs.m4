changequote dnl
changequote(`[',`]')dnl
changecom([], [Disable comments, that is, expand within them.])dnl
ifelse([The built-in "dnl" macro means "discard to next line".,])dnl
define([canary_version], [24])dnl
dnl
dnl
ifelse([Each macro takes one argument, the JDK version.])dnl
define([nonSystemTest_job], [dnl
  - job: nonSystemTest_jdk$1
ifelse($1,canary_version,,[    dependsOn:
      - nonSystemTest_jdk[]canary_version
      - misc
])dnl
    pool:
      vmImage: 'ubuntu-latest'
    container: mdernst/randoop-ubuntu-jdk$1:latest
    steps:
      - checkout: self
        fetchDepth: 25
      - bash: ./scripts/test-nonSystemTest.sh
        displayName: test-nonSystemTest.sh])dnl
dnl
define([systemTest_job], [dnl
  - job: systemTest_jdk$1
ifelse($1,canary_version,,[    dependsOn:
      - systemTest_jdk[]canary_version
      - misc
])dnl
    pool:
      vmImage: 'ubuntu-latest'
    container: mdernst/randoop-ubuntu-jdk$1:latest
    steps:
      - checkout: self
        fetchDepth: 25
      - bash: ./scripts/test-systemTest.sh
        displayName: test-systemTest.sh])dnl
dnl
define([misc_job], [dnl
  - job: misc
    pool:
      vmImage: 'ubuntu-latest'
    container: mdernst/randoop-ubuntu-jdkany:latest
    steps:
      - checkout: self
        fetchDepth: 25
      - bash: ./scripts/test-misc.sh
        displayName: test-misc.sh])dnl
dnl
ifelse([
Local Variables:
eval: (add-hook 'after-save-hook '(lambda () (run-command nil "make")) nil 'local)
end:
])dnl
