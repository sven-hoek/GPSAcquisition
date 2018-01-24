`ifndef _AMIDAR_DEFINITIONS_VH_
`define _AMIDAR_DEFINITIONS_VH_ 1

// Width of a word in AMIDAR
`define AMIDAR_WORD_SIZE	32

// Number of FUs in the system
`define AMIDAR_NUM_FU		4

// Number of bus layers
`define AMIDAR_NUM_BUS_LAYERS	1


// Width of FU opcodes
`define AMIDAR_OPCODE_WIDTH		7

// Width of data tags
`define AMIDAR_TAG_WIDTH		7

// Width of FU identifiers (has to be $clog2(AMIDAR_NUM_FU) )
`define AMIDAR_FU_ID_WIDTH		2

// Width of Port identifiers
`define AMIDAR_PORT_ID_WIDTH	2

// Width of the TagIncrement part of tokens
`define AMIDAR_TAGINC_WIDTH		1

// Total width of a token
`define AMIDAR_TOKEN_WIDTH		19


// FU Definitions
`define FU_IALU                 0
`define FU_HEAP                 1
`define FU_TOKENMACHINE         2
`define FU_FRAMESTACK           3

// Port Definitions 
`define PORT_A 0
`define PORT_B 1
`define PORT_C 2

`endif
