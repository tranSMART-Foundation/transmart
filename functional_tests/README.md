To run the tests:

- Run in this directory (functional_tests)
- for Firefox, download the geckodriver to drivers/geckodriver
  see drivers/README.md for the download URL
- On Firefox, do: `mvn test -Pfirefox`
- On Chrome, set the environment variable `CHROME_DRIVER` to the location of the
  chromedriver executable and then run `mvn test -Pchrome`. The driver will have
  to be
  [downloaded first](http://chromedriver.storage.googleapis.com/index.html)
  ro drivers/
- To run a single test source file do:  mvn -Dtest=LoginSpec test -Pfirefox
- To run a specific single test do: mvn -Dtest=LoginTests#testSuccessfulLogin test -Pfirefox
- To run tests and put results (as html) into a local web site:
  - prelim (only once for new target folder): mvn site
  - then: mvn surefire-report:report -Pfirefox
  - or, for one test: mvn surefire-report:report -Pfirefox -Dtest=LoginSpec
- When tests fail, check target/surefire-reports/(testname).txt
  - this file has the detailed message from the failed test
- To run against a particular server
  - mvn -Dgeb.env=firefoxoracle
  - options defined in GebConfig.groovy: browser and baseUrl for specific environment
  - may also need to modify AUTOLOGIN_ENABLED in Constants.groovy

To run "headless" one can use the non-graphics x-windows 'displays'. 
For example see runtest.sh in this directory.

There is a version mismatch somewhere in the updated pom.xml
file. Until this is fixed the error can be skipped with
export_JAVA_OPTIONS=-Xverify:none


