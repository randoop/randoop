changequote dnl
changequote(`[',`]')dnl
changecom([], [disable comments, that is, expand within them])dnl
include([defs.m4])dnl

version: 2.1

jobs:
include([../.azure/jobs.m4])dnl

workflows:
  build:
    jobs:
job_dependences(8, nonSystemTest)
job_dependences(8, systemTest)
job_dependences(11, nonSystemTest)
job_dependences(11, systemTest)
job_dependences(17, nonSystemTest)
job_dependences(17, systemTest)
job_dependences(21, nonSystemTest)
job_dependences(21, systemTest)
job_dependences(24, nonSystemTest)
job_dependences(24, systemTest)
      - misc
