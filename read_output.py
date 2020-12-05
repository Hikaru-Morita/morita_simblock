import json
from collections import OrderedDict
import pprint

with open('./simulator/src/dist/output/output.json') as f:
    df_list = json.load(f)

def print_jason():
    pprint.pprint(df_list,width=40)
    for list in df_list:
        print(list.get("kind"))

def getFlowBlock(blockID):
    count = 0
    propagation = 0
    for list in df_list:
        if list.get("kind") == "flow-block":
            content = list.get("content")
            if content.get("block-id") == blockID:
                count = count + 1
                propagation = propagation + content.get("reception-timestamp") - content.get("transmission-timestamp")
    return [count, propagation]

if __name__ == "__main__":
    for i in range(10):
        print(getFlowBlock(i))
