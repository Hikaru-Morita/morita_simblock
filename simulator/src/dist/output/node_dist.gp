set terminal png font "VL PGothic,15" enhanced 

set output "node_dist.png"

set ylabel 'Ratio[%]'
set xlabel 'Number of changed neighbor nodes at once'

set xtics 1
set xrange[0:8]
set offsets 0.5, 0.5, 0, 0  # left, right, top, bottom の順
set xtics scale 0           # 謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/node_dist.dat" with boxes linewidth 2 lc rgb "gray" notitle
