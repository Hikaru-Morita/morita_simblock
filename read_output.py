import pandas
import numpy as np
import matplotlib.pyplot as plt

# 全体行数/2 を指定　chunksize*NUM_START=全体行数
threshold = 5

if __name__ == "__main__":
    chunk_count = 0
    plot = []

    average_fig = plt.figure()
    average = []

    for para in range(1,10):
        # fname = './simulator/src/dist/output/individual/'+ str(para) +'.csv'

        # fname = '/home/moritta/Documents/simblock_result/shudo/default_2015/'+ str(para) +'.csv'
        # fname = '/home/moritta/Documents/simblock_result/my/default_2015/'+ str(para) +'.csv'
        fname = '/home/moritta/Documents/simblock_result/shudo/3times_2019/'+ str(para) +'.csv'
        # fname = '/home/moritta/Documents/simblock_result/my/3times_2019/'+ str(para) +'.csv'

        reader = pandas.read_csv(fname, chunksize=50)

        for chunk in reader:
            chunk_count = chunk_count + 1
            # print(chunk.iloc[10,:].values)

            # chunk's stracture is (chunksize, 1002)
            if chunk_count > threshold:
                plot.append(np.average([int(i) for i in chunk.iloc[:,500].values]))

        # 各パラメータの中央値平均値
        average.append(np.average(plot))
        print('para:{0}  {1}'.format(para,np.average(plot)))

    plt.hist(plot)
    average_fig.savefig("./simulator/src/dist/output/fig/average.png")      

    # plt.hist(plot)
    # fig.savefig("test2.png")