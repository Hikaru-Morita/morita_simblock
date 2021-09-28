set terminal png font "VL PGothic,15" enhanced
set output "multiplot_test.png"

set multiplot layout 1,2 rowsfirst downwards title "Lissajous curve"

# set ylabel 'ブロック伝播時間中央値[ms]'
# set xlabel '受信ブロック数'

# set yrange[0:11000]
# set xtics 20
# set xtics scale 0   #謎の出っ張りを消す

# set style data histogram
# set style histogram cluster gap 1
# set style fill solid border lc rgb 'black'
# set boxwidth 0.5 relative

# plot "~/Desktop/2_8.dat" with boxes linewidth 2 lc rgb "gray" title "outbound数:2 inbound数:8"
# plot "~/Desktop/8_30.dat" with boxes linewidth 2 lc rgb "gray" title "outbound数:8 inbound数:32"
# plot "~/Desktop/8_50.dat" with boxes linewidth 2 lc rgb "gray" title "outbound数:8 inbound数:52"

# set xtics scale 0   #謎の出っ張りを消す

set style data histogram

set yrange[0:11000]
set xtics 20
set xtics scale 0   #謎の出っ張りを消す

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative

plot "~/Desktop/8_30.dat" with boxes lc rgb "gray" title "outbound数:8 inbound数:32"
plot "~/Desktop/8_50.dat" with boxes lc rgb "gray" title "outbound数:8 inbound数:52"

unset multiplot