import pandas
import matplotlib.pyplot as plt

# 全体行数/2 を指定　chunksize*NUM_START=全体行数
NUM_START = 50

fname = './simulator/src/dist/output/individual_bpt.csv'
reader = pandas.read_csv(fname, chunksize=300)

if __name__ == "__main__":
    chunk_count = 0

    for chunk in reader:
        bpt_count = 0
        chunk_count = chunk_count + 1
        print(chunk.iloc[2,:].values)
        plot=chunk.iloc[2,:]

        # chunk's stracture is (100, 1002)
        if chunk_count == NUM_START:
            for bpt in chunk:
                meadian_bpt = 1

    fig = plt.figure()
    plt.hist(plot,bins=20)
    fig.savefig("test.png")