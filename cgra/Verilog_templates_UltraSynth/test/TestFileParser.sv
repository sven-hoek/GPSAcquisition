`ifndef INCLUDE_TESTFILEPARSER_SV
`define INCLUDE_TESTFILEPARSER_SV

`include "data_structures.sv"

virtual class TestFileParser;
	// 't_*' a generic token, 't_i_*' an identifier token (e.g. 'pe:'), 't_v_*' a value token (e.g. a number, or an operation specifier)
	typedef enum { 	
		t_begin, t_end, t_separator, t_eof, t_err, // delimiter/stop tokens
		t_i_pe, t_i_offset, t_i_val, t_i_hybrid, t_v_number, // parameter file parsing, some also used for test sequence parsing
		t_i_id, t_i_target, t_i_newState, t_v_string, t_i_setupType // test sequence parsing
	} Token;

	protected integer m_file;
	protected string m_fileName;
	protected Token m_currentToken;
	protected reg [9-1:0] m_currentChar;
	protected string m_currentIdentifier;
	protected integer m_currentNumber;

	function void setFile(input string fileName);
		m_fileName = fileName;
	endfunction : setFile


	function string file();
		return m_fileName;
	endfunction : file


	protected function new(input string fileName);
		m_fileName = fileName;
		m_currentToken = t_eof;
		m_currentChar = 9'h1ff;
		m_currentIdentifier = "";
		m_currentNumber = -1;
	endfunction : new


	pure virtual function void parse();
	

	protected function integer expectToken(input Token token);
		nextToken();
		if (m_currentToken == token)
			return 1;
		else
			$error("Did not find expected token %s, instead found %s (while parsing %s)", 
							tokenToString(token), tokenToString(m_currentToken), m_fileName);
		return 0;
	endfunction : expectToken


	protected function void nextToken();
		m_currentIdentifier = "";
		if (m_currentToken != t_v_number && m_currentToken != t_v_string) 
			m_currentChar = $fgetc(m_file); // value tokens did already take the next char by accident

		while (isWhiteSpace()) 
		begin 
			m_currentChar = $fgetc(m_file);
		end

		case (m_currentChar)
			"{": m_currentToken = t_begin;
			"}": if (expectChar(";")) m_currentToken = t_end; else m_currentToken = t_err;
			",": m_currentToken = t_separator;
			9'h1ff: m_currentToken = t_eof; 
			default: m_currentToken = scanAlphaNum();
		endcase
	endfunction : nextToken


	protected function string tokenToString(input Token token);
		case (m_currentToken) 
			t_begin: return "'{'";
			t_end: return "'};'";
			t_separator: return "','";
			t_eof: return "'EOF'";
			t_err: return "'error'";
			t_i_pe: return "'pe:'";
			t_i_offset: return "'offset:'";
			t_i_val: return "'value:'";
			t_i_hybrid: return "'hybrid:'";
			t_v_number: return "'number'";
			t_i_id: return "'id:'";
			t_i_target: return "'target:'";
			t_i_newState: return "'newState:'";
			t_v_string: return "'string'";
			t_i_setupType: return "'setupType:'";
			default: return "'Not a token!'";
		endcase
	endfunction : tokenToString


	local function Token scanAlphaNum();
		Token token = scanNum();
		if (t_err == token)
			token = scanAlpha();
		return token;
	endfunction : scanAlphaNum


	local function Token scanNum();
		integer i = 0;
		while ("0" == m_currentChar || "1" == m_currentChar ||
					 "2" == m_currentChar || "3" == m_currentChar ||
					 "4" == m_currentChar || "5" == m_currentChar ||
					 "6" == m_currentChar || "7" == m_currentChar ||
					 "8" == m_currentChar || "9" == m_currentChar )
		begin 
			m_currentIdentifier = {m_currentIdentifier, m_currentChar[8-1:0]};
			m_currentChar = $fgetc(m_file);
			++i;
		end
		m_currentNumber = m_currentIdentifier.atoi();

		if (i == 0)
			return t_err;
		else 
			return t_v_number;
	endfunction : scanNum


	protected virtual function Token scanAlpha();
		while ( ":" != m_currentChar && "," != m_currentChar &&
					  "}" != m_currentChar && !isWhiteSpace() ) 
		begin // stops on any string delimiter
			m_currentIdentifier = {m_currentIdentifier, m_currentChar[8-1:0]};
			m_currentChar = $fgetc(m_file);
		end
		return t_err;
	endfunction : scanAlpha


	local function integer expectChar(input byte char);
		m_currentChar = $fgetc(m_file);
		return m_currentChar == char;
	endfunction : expectChar

	local function integer isWhiteSpace();
		if ( // space, tab, line feed, carriage return
			m_currentChar == 9'h020 || m_currentChar == 9'h009 || 
			m_currentChar == 9'h00a || m_currentChar == 9'h00d
		)
			return 1;
		else
			return 0;
	endfunction : isWhiteSpace

endclass : TestFileParser

class IDTableEntryParser extends TestFileParser;

	local entryList m_entries;
	local variableList m_variables;
	local integer m_indexNewEntry;

	function new(input string fileName);
		super.new(fileName);
		m_entries = {};
		m_variables = {};
		m_indexNewEntry = 0;
	endfunction : new

	function void parse();
		m_file = $fopen(m_fileName,"r");

		nextToken();
		while (m_currentToken == t_begin) begin
			int correct = 1;
			idc_entry entry;
			var_to_write varWrite;
			correct = expectToken(t_i_pe);
			correct = expectToken(t_v_number);
			entry.pe = m_currentNumber;
			correct = expectToken(t_separator);
			correct = expectToken(t_i_offset);
			correct = expectToken(t_v_number);
			entry.offset = m_currentNumber;
			correct = expectToken(t_separator);
			correct = expectToken(t_i_val);
			correct = expectToken(t_v_number);
			varWrite.value = m_currentNumber;
			correct = expectToken(t_separator);
			correct = expectToken(t_i_hybrid);
			correct = expectToken(t_v_number);
			varWrite.hybrid = m_currentNumber;
			correct = expectToken(t_end);

			if (correct) begin 
				int id;
				for (id = 0; id < m_entries.size(); id++)
					if (m_entries[id].pe == entry.pe && m_entries[id].offset == entry.offset)
						break;
				if (id == m_entries.size()) begin 
					$display("found entry pe: %0d, offset: %0d", entry.pe, entry.offset);
					m_entries.push_back(entry);
				end
				varWrite.id = id;
				$display("found variable write id: %0d, value: %0d", varWrite.id, varWrite.value);
				m_variables.push_back(varWrite);
			end

			nextToken();
		end
		if (m_currentToken != t_eof)
			$error("Expected but did not find EOF while parsing an ID table entry file!");

		$fclose(m_file);
	endfunction : parse


	local function Token scanAlpha();
		Token token = super.scanAlpha();

		if (m_currentIdentifier == "pe")
			token = t_i_pe;
		else if (m_currentIdentifier == "offset")
			token = t_i_offset;
		else if (m_currentIdentifier == "value")
			token = t_i_val;
		else if (m_currentIdentifier == "hybrid")
			token = t_i_hybrid;
		else
			token = t_err;

		return token;
	endfunction : scanAlpha


	function entryList newEntries();
		entryList newEntriesQueue = {};
		while (m_indexNewEntry < m_entries.size()) begin 
			newEntriesQueue.push_back(m_entries[m_indexNewEntry++]);
		end
		return newEntriesQueue;
	endfunction : newEntries


	function variableList varsToWrite();
		variableList vars = m_variables;
		m_variables = {};
		return vars;
	endfunction : varsToWrite

endclass

class TestSequenceParser extends TestFileParser;

	local testList m_testSequence;

	function new(input string fileName);
		super.new(fileName);
		m_testSequence = {};
	endfunction : new

	function void parse();
		int correct = 1;
		m_file = $fopen(m_fileName,"r");

		correct = expectToken(t_begin);
		nextToken();

		while (m_currentToken == t_begin) begin
			testItem test;
			int hasType = 1;
			correct = expectToken(t_v_string);
			test.operation = m_currentIdentifier;

			if ("check" == m_currentIdentifier) begin 
				hasType = 0; // skip everything, expect only t_end
			end else begin
				correct = expectToken(t_separator); // there should be more, expect a separator 
				case (test.operation)
					"t_pe": begin 
						correct = expectToken(t_i_id);
						correct = expectToken(t_v_number);
						test.id = m_currentNumber;
						test.moduleName = $sformatf("PE%0d", m_currentNumber);
						correct = expectToken(t_separator);
					end
					"t_other": begin 
						correct = expectToken(t_i_target);
						correct = expectToken(t_v_string);
						test.moduleName = m_currentIdentifier;
						correct = expectToken(t_separator);
						correct = expectToken(t_i_id);
						correct = expectToken(t_v_number);
						test.id = m_currentNumber;
						correct = expectToken(t_separator);
					end
					"t_param": begin
						test.moduleName = "ParameterBuffer";
						test.id = 0;
					end
					"t_single": begin 
						correct = expectToken(t_i_target);
						correct = expectToken(t_v_string);
						test.moduleName = m_currentIdentifier;
						correct = expectToken(t_separator);
						correct = expectToken(t_i_id);
						correct = expectToken(t_v_number);
						test.id = m_currentNumber;
						correct = expectToken(t_separator);
					end
					"t_state": begin
						correct = expectToken(t_i_newState);
						correct = expectToken(t_v_number);
						test.id = m_currentNumber;
						correct = expectToken(t_separator);
						correct = expectToken(t_i_offset);
						correct = expectToken(t_v_number);
						test.anything = m_currentNumber;
						correct = expectToken(t_separator);
						correct = expectToken(t_i_hybrid);
						correct = expectToken(t_v_number);
						test.hybrid = m_currentNumber;
						correct = expectToken(t_separator);
						test.moduleName = "SyncUnit";
					end
					"t_master": begin
						correct = expectToken(t_i_target);
						correct = expectToken(t_v_string);
						test.moduleName = m_currentIdentifier;
						hasType = 0;
					end
					"wait": begin 
						correct = expectToken(t_v_number);
						test.anything = m_currentNumber;
						hasType = 0;
					end
					default: begin 
						$error("Expected operation specifier but found '%s'", m_currentIdentifier);
					end
				endcase
			end

			if (hasType) begin 
				correct = expectToken(t_i_setupType);
				correct = expectToken(t_v_number);
				test.setupType = SetupType'(m_currentNumber);
			end
			correct = expectToken(t_end);

			if (correct) begin 
				$display("Found test item with operation '%s' and target name '%s'.", test.operation, test.moduleName);
				m_testSequence.push_back(test);
			end
			nextToken();
		end

		if (m_currentToken != t_end)
			$error("Expected but did not find token 't_end' for a complete setup sequence!");
		
		correct = expectToken(t_eof);

		if (correct == 1)
			$display("Parsed setup sequence successfully!",);

		$fclose(m_file);
	endfunction : parse


	local function Token scanAlpha();
		Token token = super.scanAlpha();

		if (m_currentIdentifier == "id")
			token = t_i_id;
		else if (m_currentIdentifier == "type")
			token = t_i_setupType;
		else if (m_currentIdentifier == "target")
			token = t_i_target;		
		else if (m_currentIdentifier == "addr")
			token = t_i_offset;
		else if (m_currentIdentifier == "newState")
			token = t_i_newState;
		else if (m_currentIdentifier == "hybrid")
			token = t_i_hybrid;
		else
			token = t_v_string;

		return token;
	endfunction : scanAlpha


	function testList setupSequence();
		return m_testSequence;
	endfunction : setupSequence

endclass : TestSequenceParser

`endif // INCLUDE_TESTFILEPARSER_SV 