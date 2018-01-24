#############
# IP Settings
#############

set design cgra
set topLevelName Dummy

set projdir ./ultrasynth_cgra/

# should point to the CGRA directory
set root "."

# FPGA device
set partname "xc7z045ffg900-2"

# Board part
set boardpart ""

# hdl files describing the cgra composition
set hdl_files [list $root]

set ip_files [list $root/axi_cdc_fifo.xcix $root/axi_clock_converter.xcix]

set constraints_files [list $root/constraints.xdc $root/AXI_constraints.xdc]

# Other variables
# set clk_cgra "CGRA_CLK_I"
# set clk_axi "AXI_ACLK_I"

###########################
# Create Project
###########################

create_project -force $design $projdir -part $partname 
set_property target_language Verilog [current_project]
set_property source_mgmt_mode None [current_project]

if {$boardpart != ""} {
	set_property "board_part" $boardpart [current_project]
}

##########################################
# Create filesets and add files to project
##########################################

#HDL
if {[string equal [get_filesets -quiet sources_1] ""]} {
	create_fileset -srcset sources_1
}

add_files -norecurse -fileset [get_filesets sources_1] $hdl_files

set_property top $topLevelName [get_filesets sources_1]

#CONSTRAINTS
if {[string equal [get_filesets -quiet constrs_1] ""]} {
  	create_fileset -constrset constrs_1
}
if {[llength $constraints_files] != 0} {
	add_files -norecurse -fileset [get_filesets constrs_1] $constraints_files
}

#ADDING IP
if {[llength $ip_files] != 0} {
    #Add to fileset
    add_files -norecurse -fileset [get_filesets sources_1] $ip_files
    #RERUN/UPGRADE IP
    upgrade_ip [get_ips]
}