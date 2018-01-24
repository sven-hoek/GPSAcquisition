#!/bin/bash
green=$(tput setaf 2)
normal=$(tput sgr0)

# pulls from remote repo
function check_git {
	printf "${green}Checking git ...${normal}\n"
	git pull
}

if [ "$1" != "nogit" ]; then
	check_git
fi
printf "${green}Compiling ...${normal}\n"
ant
printf "${green}Done${normal}\n"