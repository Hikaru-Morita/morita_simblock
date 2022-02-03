# set terminal png font "VL PGothic,15" enhanced

# set terminal pngcairo monochrome enhanced font "VL PGothic,15"
set terminal pngcairo size 1280, 960 enhanced font "VL PGothic,15"

set output "pre_exp.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'ブロック伝播時間中央値[sec]'
set xlabel '評価間隔 [ブロック伝播回数]'

set multiplot layout 2,1

# set yrange[:7.5]
# set xrange[60:140]
set xtics 10
set xtics scale 0   #謎の出っ張りを消す

# set style data histogram
# set style histogram cluster gap 1
# set style fill solid border lc rgb 'gray30'
# set boxwidth 0.5 relative

# set key left outside

plot "~/Desktop/pre_exp.dat" using 1:2 with lines linewidth 2 lc rgb "black" title "ノード数:1000, 2015年の通信帯域" \
, "~/Desktop/pre_exp.dat" using 1:3 with lines dt (10,5) linewidth 2 lc rgb "black" title "ノード数:7000, 2015年の通信帯域" \

set yrange[:9]
# set xrange[60:140]
set xtics 10
set xtics scale 0   #謎の出っ張りを消す

# set style data histogram
# set style histogram cluster gap 1
# set style fill solid border lc rgb 'gray30'
# set boxwidth 0.5 relative

# set key left outside

plot "~/Desktop/pre_exp.dat" using 1:2 with lines linewidth 2 lc rgb "black" title "ノード数:1000, 2015年の通信帯域" \
, "~/Desktop/pre_exp.dat" using 1:4 with lines dt (10,5) linewidth 2 lc rgb "black" title "ノード数:7000, 2015年の通信帯域" \


unset multiplot