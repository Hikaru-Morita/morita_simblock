import pandas
import numpy as np
import matplotlib.pyplot as plt

# 全体行数/2 を指定　chunksize*NUM_START=全体行数
threshold = 50

if __name__ == "__main__":
    chunk_count = 0
    plot = []

    for para in range(1,10):
        fname = './simulator/src/dist/output/individual/'+ str(para) +'.csv'
        reader = pandas.read_csv(fname, chunksize=50)


        for chunk in reader:
            bpt_count = 0
            chunk_count = chunk_count + 1
            # print(chunk.iloc[10,:].values)

            plot.append(np.average([int(i) for i in chunk.iloc[:,500].values]))

            # chunk's stracture is (chunksize, 1002)
            if chunk_count >= threshold:
                for bpt in chunk:
                    meadian_bpt = 1

        # 各パラメータの中央値平均値
        print('para:{0}  {1}'.format(para,np.average(plot)))

            # fig = plt.figure()
            # plt.hist(plot,bins=20)
            # fig.savefig("./simulator/src/dist/output/fig/"+ str(para)+".png")

    # plt.hist(plot)
    # fig.savefig("test2.png")