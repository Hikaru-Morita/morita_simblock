set terminal png font "VL PGothic,15" enhanced 

set output "change_connection_num_2_8.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'Median block propagation time[sec]'
set xlabel 'Update interval[blocks]'

set yrange[0:11]
set xtics 5
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/2_8.dat" with boxes linewidth 2 lc rgb "gray" title "outbounds:2 inbounds:8"
