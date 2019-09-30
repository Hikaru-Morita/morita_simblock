import os, sys

def fileEdit(fname, index1, index2, write1, write2):
	f1=open(fname, 'r')
	tmp_list =[]
	for row in f1:
		if row.find(index1) != -1:
			tmp_list.append(write1)
		elif row.find(index2) != -1:
			tmp_list.append(write2)
		else:
			tmp_list.append(row)
	f1.close

	f2 = open(fname, 'w')
	for i in range(len(tmp_list)):
		f2.write(tmp_list[i])
	f2.close


if __name__ == '__main__':  #このファイルを本体として実行した場合、mainが実行される。
	args = sys.argv
	with open('/home/hikaru-morita/simblock-0.7.0/simulator/src/dist/output/AverageBFT.txt', 'a') as f:
		f.write('\n')
	fname = '/home/hikaru-morita/simblock-0.7.0/simulator/src/main/java/SimBlock/node/Score.java'
	index1 = 'public static double para ='
	write1 = '\tpublic static double para = ' + str(int(args[1])/10) + ';\n'  #最後の\nは改行コード
	index2 = 'ddd ='
	write2 = 'ddd = N/A\n'
    
	fileEdit(fname, index1, index2, write1, write2)
