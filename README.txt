/////////////////////////////////////////////////////////////////////////////////////
///// What to add to the build path of Eclipse projects:                        /////
/////////////////////////////////////////////////////////////////////////////////////
(Right click on project in Package Explorer > Build Path > Configure Build Path)

Amidar (Java 1.8): 		- All jar-files in AmidarTools/lib
				- The projects AmidarTools and AXTLoader
				- Java 1.8

AmidarTools (Java 1.8) :	- All jar-files in AmidarTools/lib
				- Java 1.8

API (Java 1.4):			- Nothing
	
Applications (Java 1.4):	- The project API

AXTLoader (Java 1.8):		- Java 1.8

CGRA (Java 1.8)			-

Synthesis (Java 1.8):		- json-simple-1.1.1.jar in AmidarTools/lib
				- The project AmidarTools
				- Java 1.8

/////////////////////////////////////////////////////////////////////////////////////
///// Builders in Eclipse projects:                                             /////
/////////////////////////////////////////////////////////////////////////////////////
(Right click on project in Package Explorer > Properties. Then select Builders)

For API and Applications deselect Java Builder and click "New..." and 
create a new Ant Builder. Browse the Filesystem for <amidar-sim2>/build.xml
Select the targets api or apps for the first three entries and apiclean or
appsclean for the last entry.

All other projects need no change

/////////////////////////////////////////////////////////////////////////////////////
///// How to start Amidar Simulator from Eclipse:                               /////
/////////////////////////////////////////////////////////////////////////////////////
(Run > Run Configurations...)


Execute amidar.AmidarSimulator in project Amidar with the following options entered 
in the Arguments field:
	-simple           <configFile> <pathToApplication> <synthesis(true/false)>
	-simpleSpeedup    <configFile> <pathToApplication>
	...

Example: -simple config/amidar.json de/amidar/SimpleTest false

/////////////////////////////////////////////////////////////////////////////////////
///// How to start Amidar Simulator from Commandline:                           /////
/////////////////////////////////////////////////////////////////////////////////////

Go to amidar-sim2/Amidar

The command "ant run" runs the simulator with the following default options:
	-simple config/amidar.json de/amidar/SimpleTest false

Parameters that can be changed are:
	-runtype: -simple, -simpleSpeedup, ...
	-config
	-application
	-synthesis

Example (using default application + default  config):
	ant run -Druntype="-simpleSpeedup"
