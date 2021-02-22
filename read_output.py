import pandas
import numpy as np
import matplotlib.pyplot as plt

# 全体行数/2 を指定　chunksize*NUM_START=全体行数
threshold = 50

fname = './simulator/src/dist/output/test.csv'
reader = pandas.read_csv(fname, chunksize=100)

if __name__ == "__main__":
    chunk_count = 0

    for chunk in reader:
        bpt_count = 0
        chunk_count = chunk_count + 1
        # print(chunk.iloc[10,:].values)

        print(chunk)

        # [:,500]
        plot=[int(i) for i in chunk.iloc[5,2:].values]
        print(np.average(plot))

        # chunk's stracture is (chunksize, 1002)
        if chunk_count >= threshold:
            for bpt in chunk:
                meadian_bpt = 1

    fig = plt.figure()
    plt.hist(plot,bins=20)
    fig.savefig("./simulator/src/dist/output/fig/test.png")

    plt.hist(plot)
    # fig.savefig("test2.png")