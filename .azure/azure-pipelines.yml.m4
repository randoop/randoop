changequote dnl
changequote(`[',`]')dnl
changecom([], [disable comments, that is, expand within them])dnl
include([defs.m4])dnl

trigger:
  branches:
    include:
      - '*'
pr:
  branches:
    include:
      - '*'

jobs:
include([jobs.m4])dnl
