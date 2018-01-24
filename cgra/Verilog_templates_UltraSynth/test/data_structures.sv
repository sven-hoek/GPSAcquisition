`ifndef INCLUDE_DATA_STRUCTURES_SV
`define INCLUDE_DATA_STRUCTURES_SV

typedef enum { ADDR_FRIST, DATA_FIRST, ADDR_DATA } SetupType;

typedef struct {
	integer pe;
	integer offset;
} idc_entry;

typedef struct {
	integer id;
	integer value;
	integer hybrid;
} var_to_write;

typedef struct {
	string operation;
	integer id;
	integer anything;
	integer hybrid;
	string moduleName;
	SetupType setupType;
} testItem;

typedef idc_entry entryList[$];
typedef var_to_write variableList[$];
typedef testItem testList[$];

function idc_entry makeIDCEntry(input int peNo, input int rfOffset);
	automatic idc_entry entry;
	entry.pe = peNo;
	entry.offset = rfOffset;
	return entry;
endfunction : makeIDCEntry

function var_to_write makeVarToWrite(input int id, input int value, input int hybrid);
	automatic var_to_write variable;
	variable.id = id;
	variable.value = value;
	variable.hybrid = hybrid;
	return variable;
endfunction : makeVarToWrite

`endif // INCLUDE_DATA_STRUCTURES_SV 
