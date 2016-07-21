set terminal png size 3000,1000
set xtics font ",16"
set ytics font ",18"
set key font ",20"
set xdata time
set timefmt '%Y-%m-%d %H:%M:%S'
set xrange ["%START_TIME":"%END_TIME"]
set datafile sep ','
#set title '%STRATEGY results, %VENUE' offset 0,-0.5
set ylabel 'profit/loss, %'
set ylabel font ",18"
set xlabel ''
set grid lc rgb '#cfcfcf' lt 1 lw 2
set xtics format "%Y-%m-%d\n%H:%M" norotate
#set key off
set key right bottom Right
set multiplot
set key invert
plot '%DATA_FILE' using 1:($3 * 100) with steps lt rgb "#D0D0D0" lw 4 title "Buy and hold",\
     '%DATA_FILE' using 1:($2 * 100) with steps lt rgb "#65B4E9" lw 4 title "Strategy performance"
unset multiplot
