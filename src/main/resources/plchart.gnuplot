set terminal svg enhanced size 1200,400
set tics font ",8"
set xdata time
set timefmt '%Y-%m-%d %H:%M:%S'
set xrange ["%START_TIME":"%END_TIME"]
set datafile sep ','
#set title '%STRATEGY results, %VENUE' offset 0,-0.5
set ylabel 'profit/loss, %'
set xlabel ''
set grid lc rgb '#dadada' lt 1 lw 0.5
set xtics format "%Y-%m-%d\n%H:%M" norotate
set key off
set multiplot
set autoscale yfix
plot '%DATA_FILE' using 1:($3 * 100) with steps lt rgb "#D0D0D0" lw 1.5 notitle,\
     '%DATA_FILE' using 1:($2 * 100) with steps lt rgb "#65B4E9" lw 1.5 notitle
unset multiplot
