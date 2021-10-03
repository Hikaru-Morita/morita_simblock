set terminal png font "VL PGothic,15" enhanced 

set output "propagation_compare_3times.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'Median block propagation time[sec]'
set xlabel 'Parameter P'

set yrange[0:18]
set xtics 0.1
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/propagation_compare_3times.dat" with boxes linewidth 2 lc rgb "gray" notitle \
, 8.4 with line linecolor 'red' linewidth 2 title "proposal method" \
, 10.52 with line linecolor 'blue' linewidth 2 title "Default"
