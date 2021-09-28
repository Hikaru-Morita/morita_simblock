set terminal png font "VL PGothic,15" enhanced 

set output "node_dist.png"

# set tics font "Times New Roman, 12"
# set xlabel font "Times New Roman, 12"
# set ylabel font "Times New Roman, 12"
# set key font "Times New Roman, 12“

set ylabel 'Ratio[%]'
set xlabel 'Number of changed neighbor nodes at once'

set xtics 1
set xrange[-1:9]
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/node_dist.dat" with boxes linewidth 2 lc rgb "gray" notitle
plot "~/Desktop/node_dist.dat" with boxes linewidth 2 lc rgb "gray" notitle