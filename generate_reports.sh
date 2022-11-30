#!/bin/bash

exec java -jar /Users/ab732698/Downloads/jacoco-0.8.8/lib/jacococli.jar report /Users/ab732698/Downloads/systemtests/jacoco.exec --classfiles /Users/ab732698/Downloads/systemtests/target --html /Users/ab732698/Downloads/systemtests/report


#for i in $(ls -d $SYSTEM_TESTS_COVERAGE_LOCATION/logs/*/); do
#  # Remove Last / from the folder location
#  i=`echo $i | sed 's/.$//'`
#  # Trigger report generation
#  echo "Trigger report generation for "$i
#  #exec java -jar $SYSTEM_TESTS_COVERAGE_LOCATION/jars/jacococli.jar report $i/jacoco.exec --classfiles $i/target --html $i/report
#done