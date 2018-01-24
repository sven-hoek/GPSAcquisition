onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/CGRA_CLK
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/RST_N_I
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_WREADY
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_WVALID
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_WLAST
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_AWVALID
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_AWREADY
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/S_AXI_AWADDR
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/data_before_addr
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/cgra_is_executing
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/not_during_exec
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/last_write
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/transfer_cntr
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/direct_en_initial
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/direct_en
add wave -noupdate /tb/cgra/axi_slave_buf/bound_tb_SVA_ComUnit/buffer_read_en
TreeUpdate [SetDefaultTree]
quietly wave cursor active 1
configure wave -namecolwidth 343
configure wave -valuecolwidth 389
configure wave -justifyvalue left
configure wave -signalnamewidth 0
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
configure wave -timelineunits ns
update
WaveRestoreZoom {200ns} {400ns}
