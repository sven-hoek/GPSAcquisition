#!/bin/bash
green=$(tput setaf 2)
normal=$(tput sgr0)

function run {
	printf "${green}Starting Program ...${normal}\n"
	printf "${green}Using Command '${comUltra}' with arguments ${argTest}, $1${normal}\n"
	java -cp bin:lib/* main.Main ${comUltra} ${argTest} $1
	printf "${green}Done${normal}\n"
	printf "${green}Starting Program for Test Run ...${normal}\n"
	printf "${green}Using Command '${comExec}' with argument $1${normal}\n"
	java -cp bin:lib/* main.Main ${comExec} $1
	printf "${green}Done${normal}\n"
}

comUltra=$"-ultra"
comExec=$"-exec"
argTest=$"tb"

if [ $# -eq 1 ]; then
	# use exactly one argument, if one was supplied
	printf "${green}Running $1:${normal}\n"
	run $1
else
	# run all tests if no (or an invalif amount of) args was supplied
	printf "${green}Running all:${normal}\n"
	run ultrasynth16
	run ultrasynth64
	run ultrasynth4
fi
