set terminal png size 1280,768
set tics font ",8"
set xdata time
set timefmt '%Y-%m-%d %H:%M:%S'
set xrange ["%START_TIME":"%END_TIME"]
set datafile sep ','
set title '%STRATEGY results, %VENUE' offset 0,-0.5
set ylabel 'profit/loss, %'
set xlabel '%START_DATE–%END_DATE'
set grid lc rgb '#eeeeee' lt 1 lw 0.5
set xtics format "%Y-%m-%d\n%H:%M" norotate
set key off
set multiplot
plot '%DATA_FILE' using 1:($3 / %SCALE) with steps lt rgb "#D0D0D0" lw 1,\
     '%DATA_FILE' using 1:($2 / %SCALE) with steps lt rgb "#65B4E9" lw 1
unset multiplot
