#!/bin/bash

for i in $(ls -d $SYSTEM_TESTS_COVERAGE_LOCATION/logs/*/); do
  # Remove Last / from the folder location
  i=`echo $i | sed 's/.$//'`
  # Trigger report generation
  echo "Trigger report generation for "$i
  #exec java -jar $SYSTEM_TESTS_COVERAGE_LOCATION/jars/jacococli.jar report $i/jacoco.exec --classfiles $i/target --html $i/report
done