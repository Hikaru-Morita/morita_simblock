set terminal pdfcairo color font "VL PGothic,20" enhanced 
set output 'test.eps'

set tics font "Times New Roman, 12"
set xlabel font "Times New Roman, 12"
set ylabel font "Times New Roman, 12"
set key font "Times New Roman, 12“

set ylabel 'ブロック伝播時間中央値[ms]'
set xlabel 'パラメータ P'

set yrange [0:14000]
set xtics 0.1

set style data histogram
set style histogram cluster gap 1
set style fill solid border lc rgb 'black'
set boxwidth 0.5 relative


plot "~/Desktop/result.dat" with boxes linewidth 2 lc rgb "gray" title "PNS" , "~/Desktop/my_result.dat" with lines dt (10,15) linewidth 2 lc rgb "black" title "提案方式" 