onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate /tb/cgra/ocm_buffer
add wave -noupdate /tb/cgra/log_buffer
TreeUpdate [SetDefaultTree]
quietly wave cursor active 1
configure wave -namecolwidth 300
configure wave -valuecolwidth 100
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
