To run the tests:

- On Firefox, do: `mvn test -Pfirefox`
- On Chrome, set the environment variable `CHROME_DRIVER` to the location of the
  chromedriver executable and then run `mvn test -Pchrome`. The driver will have
  to be
  [downloaded first](http://chromedriver.storage.googleapis.com/index.html).

To run "headless" one can use the non-graphics x-windows 'displays'. 
For example see runtest.sh in this directory.