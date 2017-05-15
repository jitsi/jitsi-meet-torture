jitsi-meet-torture
==================

# Running 
To run tests against a Jitsi-Meet instance running on `https://meet.example.com use`:

```ant test -Djitsi-meet.instance.url="https://meet.example.com"```

## Controlling which tests to run
To specify a list of tests to run, instead of the default of running all tests, set the `jitsi-meet.tests.toRun` property to a comma-separated list of class names (relative to org.jitsi.meet.test):

```ant test -Djitsi-meet.instance.url="https://meet.example.com" -Djitsi-meet.tests.toRun="MuteTest,TCPTest"```


To disable certain tests from being run set the `jitsi-meet.tests.toExclude` property:

```ant test -Djitsi-meet.instance.url="https://meet.example.com" -Djitsi-meet.tests.toExclude="EtherpadTests"```


Note that `SetupConference` will always be run as the first test, and `DisposeConference` will always be run as the last test.

# Mobile testing

## Running locally

### Start appium
 - plug your phone
 - ``export ANDROID_HOME="~/Library/Android/sdk"``
 - ``appium``
### Start tests
- create settings file from the template
``cp src/test/resources/settings.properties.template src/test/resources/settings.properties``
- fill in settings
  * mobile.participant (android or ios)
  * mobile.caps.deviceName
  * mobile.caps.app (path to apk or ipa file of jitsi-meet to test)
- start tests
``mvn test``

## Writing mobile tests
To be able to use the tests with services like AWS Device Farm which has 
limitations how they run the tests there are some general rules.
In order to be able to specify the tests to run we are using testng and
it config file testng.xml 
* The tests does not have order, priority, grouping.
* All tests to be ran are declared in src/test/resources/testng.xml 
    (just the class name)
* Tests extend org.jitsi.meet.test.mobile.base.AstractBaseTest which is 
responsible for setting up the driver.
* You cannot use static variables shared between tests and test methods.
* With all the above limitations we end up with one class 
with one test method.