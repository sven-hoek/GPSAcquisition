#!/bin/sh

cd log
echo
echo Converting GraphViz files...
echo
# if [$1 != "-g"]; then
	for i in *.tex; do echo $i; latex $i; dvips *.dvi; ps2pdf *.ps; rm -f ./*schedule.log ./*schedule.dvi ./*schedule.ps ./*schedule.aux; done
	for i in *.sch; do echo $i; dot -Kfdp -Tpdf $i -o $i.pdf; done
	for i in *.dot; do echo $i; dot -Tpdf $i -o $i.pdf; done
# fi
echo
echo
echo Converting EPS files...
echo
for i in *.eps; do echo $i; epspdf $i; done
echo
echo All done
