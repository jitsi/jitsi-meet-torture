jitsi-meet-torture
==================

# Running 
To run tests against a Jitsi-Meet instance running on `https://meet.example.com` use:

```mvn test -Djitsi-meet.instance.url="https://meet.example.com"```

## Controlling which tests to run
To specify a list of tests to run, instead of the default of running all tests, set the `jitsi-meet.tests.toRun` property to a comma-separated list of class names (relative to org.jitsi.meet.test):

```mvn test -Djitsi-meet.instance.url="https://meet.example.com" -Djitsi-meet.tests.toRun="MuteTest,TCPTest"```


To disable certain tests from being run set the `jitsi-meet.tests.toExclude` property:

```mvn test -Djitsi-meet.instance.url="https://meet.example.com" -Djitsi-meet.tests.toExclude="EtherpadTests"```


Note that `SetupConference` will always be run as the first test, and `DisposeConference` will always be run as the last test.

## Running IFrameAPITest
To run IFrameAPITest an iframe implementation is needed. You can upload the file resources/files/iframeAPITest.html to your deployment and pass it as a param:
Make sure the file is accessible as `https://meet.example.com/iframeAPITest.html`

```mvn test -Djitsi-meet.instance.url="https://meet.example.com" -Dorg.jitsi.iframe.page_path="https://meet.example.com" -Djitsi-meet.tests.toRun="IFrameAPITest"```

# Mobile testing

## Running locally

### Start appium
 - plug your phone
 - ``export ANDROID_HOME="~/Library/Android/sdk"``
 - ``appium``
### Start tests
- pass settings as params
  * mobile.participant (mobile.[android|ios])
  * mobile.deviceName
  * mobile.app (path to apk or ipa file of jitsi-meet to test)
- start tests
``mvn test -Dmobile.participant=mobile.android -Dmobile.deviceName=your_device_name -Dmobile.app=absolute/path/to/app.apk -Djitsi-meet.instance.url="https://beta.meet.jit.si" -Phybrid -Dmobile.android.reinstallApp=true``

### Package mobile tests
``mvn package -Dmobile.participant=android ... -Pmobile`` 

## Writing mobile tests
To be able to use the tests with services like AWS Device Farm which has 
limitations how they run the tests there are some general rules.
In order to be able to specify the tests to run we are using testng and
it config file testng.xml 
* The tests does not have order, priority, grouping.
* All tests to be ran are declared in src/test/resources/mobile/testng.xml 
    (just the class name)
* Tests extend org.jitsi.meet.test.mobile.base.AstractBaseTest which is 
responsible for setting up the driver.
* You cannot use static variables shared between tests.
* With all the above limitations we end up with one class 
with one test method.


## PSNR Tests
The PSNR tests will run by default if:
* The `PSNRTest.INPUT_FRAME_DIR` exists and
* The `ConferenceFixture.FAKE_VIDEO_FNAME_PROP` file exists

The `scripts/psnr-build-resources.sh` can be used to build the needed resources from a y4m file like so:
```
scripts/psnr-build-resources.sh ./FourPeople_1280x720_30.y4m
```

The `ConferenceFixture.FAKE_VIDEO_FNAME_PROP` property should point to the stamped y4m file that was created from the above script.

The test will output the calculated PSNR value for each frame, as well as a running average for all frames.  If `ConferenceFixture.PSNR_OUTPUT_DIR_PROP` and `ConferenceFixture.PSNR_OUTPUT_FILENAME_PROP` are set, the overall average PSNR value will be written to the file described by the two properties.

`scripts/push_psnr_results.py` can be invoked to push the psnr value (and some variables from the jenkins build environment) to a configured URL.
