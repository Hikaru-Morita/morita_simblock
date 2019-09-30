# 参照元
# http://peaceandhilightandpython.hatenablog.com/entry/2013/12/06/082106
import json

f = open('/home/hikaru-morita/simblock-0.7.0/simulator/src/dist/output/output.json', 'r')
jsonData = json.load(f)
jason = json.dumps(jsonData, sort_keys = True, indent = 4)
f.close()

with open('testjason.txt','w') as test:
    test.write(jason)    
