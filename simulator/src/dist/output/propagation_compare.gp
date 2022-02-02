# set terminal png font "VL PGothic,15" enhanced 
set terminal pngcairo enhanced font "VL PGothic,15"

set output "propagation_compare.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'ブロック伝播時間中央値[sec]'
set xlabel 'パラメータ P'

set yrange[0:12]
set xtics 0.1
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'gray30'
set boxwidth 0.5 relative

plot "~/Desktop/propagation_compare.dat" with boxes linewidth 2 lc rgb "gray60" notitle \
, 7.94 with line dt (10,5) linecolor 'black' linewidth 2 title "既定" \
, 6.76 with line linecolor 'black' linewidth 2 title "提案方式" \
