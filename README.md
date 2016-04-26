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
