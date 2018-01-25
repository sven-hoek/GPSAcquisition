#!/bin/bash
green=$(tput setaf 2)
normal=$(tput sgr0)

#################################################################################################
# Usage: first arg is the composition to run, sec arg specifies if a GUI should be started.		#
# Leaving out the second argument (which is always <nogui> when actually used) starts the GUI.	#
# If no args are provided, the script falls back to defaults. (Uses a GUI and runs ultrasynth4)	#
#################################################################################################

name=$"ultrasynth4"

### get the composition name (or use default) ###
if [ ! -z "$1" ]; then
	# user input was not a null string, proceed by checking if the target directory exists
	if [ -d "out/${1}" ]; then
		name=$1
	else
		echo "Provided name does not match a folder in \"out\", exiting."
		exit 1
	fi
fi
cd out/${name}

### get the number of the desired test run ###
printf "${green}Which test sequence should be executed? (type the number)${normal}\n"
read run

### prepare the test sequence ###
printf "${green}Preparing sequence '${run}' ...${normal}\n"
rm -f singleTestSeq.tf
touch singleTestSeq.tf
sed "${run}q;d" setupSequence.tf >> singleTestSeq.tf
printf "${green}Using sequence:${normal}\n"
head singleTestSeq.tf
printf "${green}Preparing .tcl ...${normal}\n"

### build the simulation script ###
rm -f manualTcl.tcl
touch manualTcl.tcl
echo "vmap work cgra_work" >> manualTcl.tcl
echo "vsim tb bind_SVA -novopt" >> manualTcl.tcl

# only add waves if this is a non gui run
if [ "$2" != "nogui" ]; then
	echo "do wave_manual.do" >> manualTcl.tcl
	echo "do wave_master.do" >> manualTcl.tcl
fi
echo "run -all" >> manualTcl.tcl

### run the simulation ###
printf "${green}Starting Simulation of '${name}' using manualTcl.tcl ...${normal}\n"

# look for further input indicating a non gui run
if [ "$2" == "nogui" ]; then
	vsim -do manualTcl.tcl &
else
	vsim -do manualTcl.tcl
fi

printf "${green}Done${normal}\n"

