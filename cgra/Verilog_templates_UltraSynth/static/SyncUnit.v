`include "ultrasynth.vh"
`include "cgra.vh"
module SyncUnit #
(
	parameter integer CONTEXT_ADDR_WIDTH = -1,
	parameter integer CYCLE_COUNTER_WIDTH = -1,
	parameter integer INCOMING_DATA_WIDTH = -1,
	parameter integer RUN_COUNTER_WIDTH = -1,
	parameter integer SPECIAL_ACTION_COUNTER_WIDTH = -1
)
(
	input wire EN_I,
	input wire CGRA_CLK_I,
	input wire RST_N_I,
	input wire STATE_CHANGE_I,
	input wire INTERVAL_CHANGE_I,
	input wire SPECIAL_ACTION_COUNTER_RESET_VAL_CHANGE_I,
	input wire [INCOMING_DATA_WIDTH-1:0] DATA_I,
	input wire SENSOR_WRITES_COMPLETE_I,
	input wire RUN_STARTED_I,
	output wire ENABLE_SPECIAL_ACTIONS_O,
	output wire TRIGGER_RUN_O,
	output wire IS_HYBRID_O,
	output wire SYNC_IN_O,
	output wire [CONTEXT_ADDR_WIDTH-1:0] START_ADDR_O,
	output wire [RUN_COUNTER_WIDTH-1:0] RUN_COUNTER
);

// declarations
reg run; // meta state of an execution cycle, tells if a CGRA EXECUTE may be triggered
reg hybrid; // specifies if CGRA and Zynq-PS are working together
// reg hybrid_trigger; // specifies that a hybrid run is about to be triggered
reg enable_special_actions; // specifies that the current cycle (of interval_length clocks) is where multirate stuff happens (e.g. parameter updates)
// reg hybrid_was_triggered; // specifies that hybrid_trigger was already asserted during this CGRA cycle
reg trigger_run; // notify CGRA to start its schedule
reg was_triggered; // limits trigger_run assertions to one per cycle 
reg sync_in; // notify the sensors buffer that it should collect data from all sensors
reg [CONTEXT_ADDR_WIDTH-1:0] start_word; // the ccnt value to start from
reg [CYCLE_COUNTER_WIDTH-1:0] interval_length; // upper boundary for cycle counter
reg [SPECIAL_ACTION_COUNTER_WIDTH-1:0] special_action_counter_reset_val; // the value to count back from

// Counters

// Counts the clock cycles of an execution cycle.
// Forces sync_in at value 0, if run is set.
reg [CYCLE_COUNTER_WIDTH-1:0] cycle_counter; 

// Counts the amount of execution cycles 
reg [RUN_COUNTER_WIDTH-1:0] run_counter; 

// Counts the amount of execution cycles which have 
// to pass before enabling some actions in the top level.
// Counts backwards!
reg [SPECIAL_ACTION_COUNTER_WIDTH-1:0] special_action_counter;

// Input spection wires.
// Encode the requested state as follows:
// h r -> (h: hybrid, r: run)
// 0 0 -> Pause, no counter reset
// 0 1 -> Run, not hybrid
// 1 0 -> Stop, reset counters
// 1 1 -> Run, hybrid
parameter 	STATE_WIDTH = 2;
parameter 	PAUSE = 0,
			RUN = 1,
			STOP = 2,
			HYBRID_RUN = 3;

wire [STATE_WIDTH-1:0] incoming_state;
wire [STATE_WIDTH-1:0] state;
wire incoming_run_bit;
wire incoming_hybrid_bit;

assign incoming_state = DATA_I[CONTEXT_ADDR_WIDTH+1:CONTEXT_ADDR_WIDTH];
assign state = {hybrid, run};
assign incoming_run_bit = DATA_I[CONTEXT_ADDR_WIDTH];
assign incoming_hybrid_bit = DATA_I[CONTEXT_ADDR_WIDTH+1];

// output assignments
assign START_ADDR_O = start_word;
assign TRIGGER_RUN_O = trigger_run;
assign IS_HYBRID_O = hybrid;
assign SYNC_IN_O = sync_in;
assign RUN_COUNTER = run_counter;
assign ENABLE_SPECIAL_ACTIONS_O = enable_special_actions;

// handle start address and state changes
always @(posedge CGRA_CLK_I) begin
	if (RST_N_I == 1'b0) begin
		// Both run and hybrid are required to be reset to 0, 
		// otherwise the CCU reset will not work properly!
		run <= 1'b0;
		hybrid <= 1'b0;	
		start_word <= {CONTEXT_ADDR_WIDTH{1'b1}};
		interval_length <= {CYCLE_COUNTER_WIDTH{1'b1}};
		special_action_counter_reset_val <= 0;
	end
	else if (EN_I) begin
		if (STATE_CHANGE_I) begin
			run <= incoming_run_bit;
			hybrid <= incoming_state == HYBRID_RUN ? 1'b1 : 1'b0;

			// We set the start_word (i.e. the CCNT value to use when starting exec)
			// only if the previous state was PAUSE or STOP
			if (~run)
				start_word <= DATA_I[CONTEXT_ADDR_WIDTH-1:0];
		end
		if (INTERVAL_CHANGE_I) 
			interval_length <= DATA_I[CYCLE_COUNTER_WIDTH-1:0];
		if (SPECIAL_ACTION_COUNTER_RESET_VAL_CHANGE_I)
			special_action_counter_reset_val <= DATA_I[SPECIAL_ACTION_COUNTER_WIDTH-1:0];
	end
end

// counter and special actions management
always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		run_counter <= 0;
		cycle_counter <= 0;
		special_action_counter <= 0;
		enable_special_actions <= 1'b1;
	end else if (EN_I) begin
		if (STATE_CHANGE_I) begin
			// Depending on the requested state we have to do some resets:
			// We possibly broke the exec cycle, reset the clock cycle counter
			// no matter what the incoming state is!
			cycle_counter <= 0;

			// When not pausing it is also required to reset the other counters.
			if (state != PAUSE) begin
				run_counter <= 0;
				special_action_counter <= special_action_counter_reset_val;
				enable_special_actions <= special_action_counter_reset_val == 0;
			end 
		end else if (cycle_counter == interval_length) begin
			run_counter <= run_counter + 1;
			cycle_counter <= 0;

			if (special_action_counter == 0) begin
				enable_special_actions <= 1'b1;
				special_action_counter <= special_action_counter_reset_val;
			end else begin
				enable_special_actions <= 1'b0;
				special_action_counter <= special_action_counter - 1;
			end
		end else if (run) begin
			cycle_counter <= cycle_counter + 1;
		end
	end
end

// run management
always @(posedge CGRA_CLK_I) begin
	if (~RST_N_I) begin
		sync_in <= 1'b0;
		trigger_run <= 1'b1; // Trigger a run on reset to force CCU to the highest address
		was_triggered <= 1'b0;
		// hybrid_trigger <= 1'b0;
		// hybrid_was_triggered <= 1'b0;
	end else if (EN_I) begin
		if (run && cycle_counter == 0)
			sync_in <= 1'b1;
		else
			sync_in <= 1'b0;

		// And'ing 'run' is required as we want to prevent the
		// assertion of was_triggered when resetting the CCU.
		if (RUN_STARTED_I && run)
			was_triggered <= 1'b1;
		else if (run && cycle_counter == 0)
			was_triggered <= 1'b0;

		// if (hybrid_trigger)
		// 	hybrid_was_triggered <= 1'b1;
		// else if (cycle_counter == 0)
		// 	hybrid_was_triggered <= 1'b0;

		// if (hybrid_trigger)
		// 	hybrid_trigger <= 1'b0;
		// else if (special_action_counter == 0 && SENSOR_WRITES_COMPLETE_I)
		// 	hybrid_trigger <= 1'b1;

		if (RUN_STARTED_I)
			trigger_run <= 1'b0;
		else if (run /*&& ~hybrid*/ && SENSOR_WRITES_COMPLETE_I && ~was_triggered)
			trigger_run <= 1'b1;
		// else if (run && hybrid && hybrid_trigger && ~was_triggered)
		// 	trigger_run <= 1'b1;
		// When triggering a run we ignore the hybrid state.
		// It remains in place (for now) as it is unknown if it may be usefull
		// in the top level.
	end
end

endmodule
