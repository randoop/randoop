changequote dnl
changequote(`[',`]')dnl
changecom([], [disable comments, that is, expand within them])dnl
include([defs.m4])dnl

trigger:
  autoCancel: true
  branches:
    include:
      - '*'
pr:
  autoCancel: true
  branches:
    include:
      - '*'

jobs:
include([jobs.m4])dnl
