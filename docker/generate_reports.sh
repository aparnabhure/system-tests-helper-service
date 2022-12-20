SYSTEM_TESTS_COVERAGE_LOCATION=/Users/ab732698/Downloads/systemtests/UAT_Phase1

for i in $(ls -d $SYSTEM_TESTS_COVERAGE_LOCATION/logs1/*/); do
  # Remove Last / from the folder location
  i=`echo $i | sed 's/.$//'`
  # Trigger report generation
  #echo "Trigger report generation for "$i
  echo java -jar $SYSTEM_TESTS_COVERAGE_LOCATION/jars/jacococli.jar report $i/jacoco.exec --classfiles $i/target --xml $i/report.xml
done