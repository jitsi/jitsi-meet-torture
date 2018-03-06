#!/usr/bin/env python2.7
#
# Copyright @ 2018 Atlassian Pty Ltd
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

import sys
import logging
import argparse
import json
import pandas as pd

pd.options.display.width = None
pd.options.display.max_rows = None
pd.options.display.max_columns = None
pd.options.display.float_format = lambda x: '%.3f' % x

def make_series(stats, time_base=None):
    times = stats['times']
    values = stats['values']
    if time_base:
        # rebase times/translate time series
        delta = time_base - min(stats['times'])
        times = [x + delta for x in times]

    return pd.Series(values, index=times, dtype=int)

def compare(args):

    jvb_stats = json.load(args.jvb)
    p2p_stats = json.load(args.p2p)

    jvb_bits = make_series(
            jvb_stats['Conn-audio-1-0-bytesReceived']) * 8
    p2p_bits = make_series(
            p2p_stats['Conn-audio-1-0-bytesReceived'],
            jvb_bits.index.min()) * 8

    p2p_bps = p2p_bits.diff()
    jvb_bps = jvb_bits.diff()

    ratio = jvb_bps.mean() / p2p_bps.mean()
    if args.loglevel == logging.DEBUG:
        logging.debug(pd.DataFrame({'p2p_bps': p2p_bps, 'jvb_bps': jvb_bps}))
    print(ratio)
    sys.exit(0 if ratio > .95 else 1)

def plot(args):
    pass

def main():
    # create the top-level parser.
    parser = argparse.ArgumentParser()
    parser.add_argument('-d', '--debug',
            action="store_const", dest="loglevel",
            const=logging.DEBUG, default=logging.WARNING)
    subparsers = parser.add_subparsers()

    # create the parser for the plot command
    plot_parser = subparsers.add_parser('plot')
    plot_parser.set_defaults(func=plot)

    # create the parser for the compare command
    compare_parser = subparsers.add_parser('compare')
    compare_parser.set_defaults(func=compare)
    compare_parser.add_argument('--p2p', type=argparse.FileType('r'), required=True)
    compare_parser.add_argument('--jvb', type=argparse.FileType('r'), required=True)

    args = parser.parse_args()
    logging.basicConfig(level=args.loglevel)
    args.func(args)

if __name__ == "__main__":
    main()

# vim: tabstop=8 expandtab shiftwidth=4 softtabstop=4
