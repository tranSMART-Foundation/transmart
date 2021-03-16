To run the tests:

- Run in this directory (functional_tests)
- drivers are downloaded automatically
- On Firefox, do: `mvn test -Pfirefox`
- To run a single test source file do:  mvn -Dtest=LoginSpec test -Pfirefox
- To run a specific single test do: mvn -Dtest=LoginSpec#"go to login screen" test -Pfirefox
- To run tests and put results (as html) into a local web site:
  - prelim (only once for new target folder): mvn site
  - then: mvn surefire-report:report -Pfirefox
  - or, for one test: mvn surefire-report:report -Pfirefox -Dtest=LoginSpec
- When tests fail, check target/surefire-reports/(testname).txt
  - this file has the detailed message from the failed test
- To run against a particular server create a profile and include in GebConfig.groovy
  - mvn -Dgeb.env=firefoxoracle
  - options defined in GebConfig.groovy: browser and baseUrl for specific environment
  - may also need to modify AUTOLOGIN_ENABLED in Constants.groovy

To run "headless" one can use the non-graphics x-windows 'displays'.
For example see runtest.sh in this directory.


