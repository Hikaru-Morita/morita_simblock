set terminal png font "VL PGothic,15" enhanced 

set output "propagation_freq_3times.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'ブロック伝播時間中央値[ms]'
set xlabel '受信ブロック数'

set yrange[0:10000]
set xtics 10
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/propagation_freq_3times.dat" with boxes linewidth 2 lc rgb "gray" notitle
