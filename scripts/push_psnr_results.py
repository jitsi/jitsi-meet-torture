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
# This script takes in the path of a psnr test output file (which should contain
# json-encoded data) as well as the url of a dashboard service
# to which the psnr, as well as some variables from a jenkins build environment,
# will be pushed.  Note that this does assume it's being executed as part of a
# jenkins build, so if run elsewhere the caller should make sure those variables
# are defined.
# Usage example:
# ./push_psnr_results.py ./psnr.out jvb_1.0 http://my.dashboard.service/psnrPushEndpoint

import os
import json
import pprint
import requests
import sys
import datetime

if len(sys.argv) < 2:
    print("Usage: %s <psnr_output_file_path> <project_name> <dashboard_service_url> \n" % (sys.argv[0]))
    sys.exit(1)

psnr_output_file_path = sys.argv[1]
project_name = sys.argv[2]
dashboard_service_url = sys.argv[3]
jenkins_build_number = os.environ["BUILD_NUMBER"]
jenkins_build_url = os.environ["BUILD_URL"]
build_date = str(datetime.datetime.now())

with open(psnr_output_file_path, "r") as f:
    json_data = json.load(f)

json_data["buildNum"] = jenkins_build_number
json_data["buildUrl"] = jenkins_build_number
json_data["buildDate"] = build_date
json_data["projectName"] = project_name

print("got psnr result data jenkins job %s, build number %s, project name %s:\n%s\n"
        % (jenkins_build_url, jenkins_build_number, project_name, pprint.pformat(json_data, indent=2)))

r = requests.post(dashboard_service_url, json=json_data)
if r.status_code != requests.codes.ok:
    print("Error pushing psnr data to dashboard: %s" % r.status_code)
    sys.exit(1)
