@echo off


for %%f in (.\*.bnd) do (
	echo %%~nf
	.\dsc2json.exe %%f > %%~nf.dsc.json 
	.\dscJson2ultraJson.exe %%~nf.dsc.json %%~nf.ultrasynth.json 
)

@echo All files done!

pause