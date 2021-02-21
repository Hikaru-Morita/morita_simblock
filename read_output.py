import json
from collections import OrderedDict
import pprint

import pandas

NUM_START = 50

fname = './simulator/src/dist/output/individual_bpt.csv'
reader = pandas.read_csv(fname, chunksize=100)

if __name__ == "__main__":
    chunk_count = 0

    for chunk in reader:
        bpt_count = 0
        chunk_count = chunk_count + 1
        print(chunk['500'])

        # chunk's stracture is (100, 1002)
        if chunk_count >= NUM_START:
            for bpt in chunk:
                meadian_bpt = 1