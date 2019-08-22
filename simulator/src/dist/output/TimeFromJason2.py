# 参照元
# http://peaceandhilightandpython.hatenablog.com/entry/2013/12/06/082106
import json
import statistics

# jsonファイルを読み込む
def load_json(filepath):
    f = open(filepath, 'r')
    jsonData = json.load(f)
    f.close()
    return jsonData

# jsonDataから"content":"flow-block"を抽出する
def getFlowBlock(jsonData):
    flow_block = []
    for dic in jsonData:
        if(dic["kind"] == "flow-block"):
            flow_block.append(dic["content"])
            # dic["content"]["begin-node-id"]
            # dic["content"]["block-id"]
            # dic["content"]["end-node-id"]
            # dic["content"]["reception-timestamp"]
            # dic["content"]["transmission-timestamp"]
    return flow_block

def getMedianTime(flow_block):
    reception_timestamp = 0
    medianTime = 0
    time = []
    for i,dic in enumerate(flow_block):
        # selfNode = dic["begin-node-id"]
        oppNode = dic["end-node-id"]
        blockID = dic["block-id"]
        transmission_timestamp = dic["transmission-timestamp"]
        for dic2 in flow_block[i:]:
            if(oppNode == dic2["begin-node-id"] and blockID == dic2["block-id"]):
                reception_timestamp = dic2["reception-timestamp"]
                time.append(reception_timestamp - transmission_timestamp)       
    medianTime = statistics.median(time)
        # print(medianTime)
    return medianTime

jsonData = load_json('/home/hikaru-morita/simblock-0.7.0/simulator/src/dist/output/output.json')
flow_block = getFlowBlock(jsonData)
print(getMedianTime(flow_block))

