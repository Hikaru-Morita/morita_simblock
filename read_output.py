import pandas
import numpy as np
import matplotlib.pyplot as plt

# 全体行数/2 を指定　chunksize*NUM_START=全体行数
threshold = 1

def plot_hist():
    fname_shudo = '/home/moritta/Documents/simblock_result/shudo/3times_2015/'+ str(9) +'.csv'
    fname_my = '/home/moritta/Documents/simblock_result/my/3times_2015/'+ str(5) +'.csv'
    # fname_shudo = './simulator/src/dist/output/individual/'+ str(4) +'.csv'

    reader_shudo = pandas.read_csv(fname_shudo, chunksize=100)
    reader_my = pandas.read_csv(fname_my, chunksize=100)

    hist_fig = plt.figure()
    chunk_count = 0
    hist_plot = []

    for chunk in reader_shudo:
        chunk_count = chunk_count + 1
        # chunk's stracture is (chunksize, 1002)
        if chunk_count > threshold:
            plot.append(np.average([int(i) for i in chunk.iloc[:,500].values]))
            if chunk_count > 1:
                for i in range(1,1000):
                    hist_plot.append(np.average([int(i) for i in chunk.iloc[:,i].values]))
    plt.tight_layout()
    plt.hist(hist_plot,bins=100, alpha=0.5)
    hist_plot=[]
    chunk_count = 0

    for chunk in reader_my:
        chunk_count = chunk_count + 1
        # chunk's stracture is (chunksize, 1002)
        if chunk_count > threshold:
            plot.append(np.average([int(i) for i in chunk.iloc[:,500].values]))
            if chunk_count > 1:
                for i in range(1,1000):
                    hist_plot.append(np.average([int(i) for i in chunk.iloc[:,i].values]))

    plt.tight_layout()
    plt.hist(hist_plot,bins=100,alpha=0.5,color='orange')
    plt.xlabel("ブロック伝播時間[ms]",fontname="IPAexGothic")
    plt.ylabel("ノード数[個]",fontname="IPAexGothic")
    plt.subplots_adjust(left=0.1,bottom=0.1)
    hist_fig.savefig('./simulator/src/dist/output/fig/conpare.png')

    return 


if __name__ == "__main__":
    hist_fig = plt.figure()
    average = []

    for para in range(1,11):
        plot = []
        hist_plot = []

        fname = './simulator/src/dist/output/individual/'+ str(para) +'.csv'

        # fname = '/home/moritta/Documents/simblock_result/shudo/default_2015/'+ str(para) +'.csv'
        # fname = '/home/moritta/Documents/simblock_result/my/default_2015/'+ str(para) +'.csv'

        # fname = '/home/moritta/Documents/simblock_result/shudo/3times_2015/'+ str(para) +'.csv'
        # fname = '/home/moritta/Documents/simblock_result/my/3times_2015/'+ str(para) +'.csv'

        reader = pandas.read_csv(fname, chunksize=100)
        chunk_count = 0

        for chunk in reader:
            chunk_count = chunk_count + 1

            # chunk's stracture is (chunksize, 1002)
            if chunk_count > threshold:
                plot.append(np.average([int(i) for i in chunk.iloc[:,500].values]))
                if chunk_count > 2:
                    for i in range(1,1000):
                        hist_plot.append(np.average([int(i) for i in chunk.iloc[:,i].values]))

        # 各パラメータの中央値平均値
        average.append(np.average(plot))
        print('para:{0}  {1}'.format(para,np.average(plot)))

        # if para == 3 or para == 8:
        plt.tight_layout()
        plt.hist(hist_plot,bins=100)
        hist_fig.savefig(('./simulator/src/dist/output/fig/para{0}_hist.png'.format(para)))

        hist_fig = plt.figure()

    
    average_fig = plt.figure()
    para=[0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9]
    plt.tight_layout()
    ax = average_fig.add_subplot(111)
    plt.bar(para,average,width=0.075,edgecolor='k',tick_label=para)
    plt.hlines(15000,0,0.9,color='g',linestyles='dashed')
    plt.hlines(8573,0,0.9,color='r',linestyles='dashed')
    average_fig.savefig("./simulator/src/dist/output/fig/average.png") 

    plot_hist()     


    # plt.hist(plot)
    # fig.savefig("test2.png")
