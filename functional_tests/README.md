To run the tests:

- On Firefox, do: `mvn test -Pfirefox`
- On Chrome, set the environment variable `CHROME_DRIVER` to the location of the
  chromedriver executable and then run `mvn test -Pchrome`. The driver will have
  to be
  [downloaded first](http://chromedriver.storage.googleapis.com/index.html).

To run "headless" one can use the non-graphics x-windows 'displays'. 
For example see runtest.sh in this directory.

For basic tests:  mvn test -Pfirefox
To run one test, for example:  mvn test -Pfirefox -Dtest=SurvivalAnalysisTests
To run tests and put results in (html) web site:
  prelim (only once for new target folder): mvn site
  then: mvn surefire-report:report -Pfirefox
  or, for one test: mvn surefire-report:report -Pfirefox -Dtest=SurvivalAnalysisTests

