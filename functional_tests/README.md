To run the tests:

- Run in this directory (functional_tests)
- On Firefox, do: `mvn test -Pfirefox`
- On Chrome, set the environment variable `CHROME_DRIVER` to the location of the
  chromedriver executable and then run `mvn test -Pchrome`. The driver will have
  to be
  [downloaded first](http://chromedriver.storage.googleapis.com/index.html).
- To run a single test source file do:  mvn -Dtest=LoginTests test -Pfirefox
- To run a specific single test do: mvn -Dtest=LoginTests#testSuccessfulLogin test -Pfirefox
- To run tests and put results (as html) into a local web site:
  - prelim (only once for new target folder): mvn site
  - then: mvn surefire-report:report -Pfirefox
  - or, for one test: mvn surefire-report:report -Pfirefox -Dtest=LoginTests

To run "headless" one can use the non-graphics x-windows 'displays'. 
For example see runtest.sh in this directory.


