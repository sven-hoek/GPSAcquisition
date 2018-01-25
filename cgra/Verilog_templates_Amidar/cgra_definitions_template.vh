/*
Author: Dennis L. Wolf
Date: 01.09.2015
Version: 1.0 (definitions - for autogeneration ultrasynth)
Version History:	1.0 Creation
*/

// check if definitions already done:
`ifndef DEFS_DONE
  `define DEFS_DONE

  `define   DATA_WIDTH 32 
  `define   CACHE_ADDR_WIDTH 32 
  `define   DATA_WIDTH_CACHE 32
  
  `define   IDLE   0 
  `define   PREPARING_CONTEXT_RECEIVE   1 
  `define   WRITING_CONTEXT_RECEIVE 2
  `define   WRITE_LOCAL_VAR  3 
  `define   PREPARING_CONTEXT_SEND   4 
  `define   WRITING_CONTEXT_SEND 5
  `define   SEND_LOCAL_VAR 6
  `define   SETADDRESS  7 
  `define   RUN  8
  `define   LOAD_PROGRAM 9
  `define   LOAD_PROGRAM_FINISHED 10

  `define   OP_RECEIVELOCALVAR   0         
  `define   OP_SENDLOCALVAR  1 
  `define   OP_RUN   2
  `define   OP_LOADPROGRAM   3  

 
//import definitions::*  // wildcard import all definitions
  
 `endif

//end of definitions
