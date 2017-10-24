#!/usr/bin/python
#
# Copyright @ 2015 Atlassian Pty Ltd
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# This script takes in the path of a psnr output file (which should contain only
# a float psnr value, and nothing else) as well as the url of a dashboard service
# to which the psnr, as well as some variables from a jenkins build environment,
# will be pushed.  Note that this does assume it's being executed as part of a
# jenkins build, so if run elsewhere the caller should make sure those variables
# are defined.
# Usage example:
# ./push_psnr_results.py ./psnr.out http://my.dashboard.service/psnrPushEndpoint

import os
import requests
import sys

if len(sys.argv) < 2:
    print("Usage: %s <psnr_output_file_path> <dashboard_service_url> \n" % (sys.argv[0]))
    sys.exit(1)

psnr_output_file_path = sys.argv[1]
dashboard_service_url = sys.argv[2]
jenkins_build_number = os.environ["BUILD_NUMBER"]
jenkins_build_url = os.environ["BUILD_URL"]

with open(psnr_output_file_path, "r") as f:
    line = f.readline()

psnr = float(line)

print("got psnr value %f for jenkins job %s, build number %s\n" % (psnr, jenkins_build_url, jenkins_build_number))

jsonData = {
    "buildNum": jenkins_build_number,
    "buildUrl": jenkins_build_url,
    "psnrValue": psnr
}

r = requests.post(dashboard_service_url, json=jsonData)
if r.status_code != requests.codes.ok:
    print("Error pushing psnr data to dashboard: %s" % r.status_code)
    sys.exit(1)
