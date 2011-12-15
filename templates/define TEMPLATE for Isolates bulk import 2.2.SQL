/** 
	Define new bulk import template for appvaluetype = IsolatesImportFile
  
   @created 7/2011 by TJS/Wildtype for SGI
 **/
 
-- select * from vwLinx_AppValues where appvaluetype like '%Isolate%'
--dbcc OPENTRAN

begin tran

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'New LIMS ID*' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Origin LIMS ID*' ,
	@tableField = 'isolate.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 2 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 

/** -
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Origin ID Type',
	@appValue = 'EnvironmentalSample',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Origin ID Type',
	@appValue = 'PrimaryEnrichment',
	@tranId = 62190,
	@appValueId = @appvalueid output

exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Origin ID Type',
	@appValue = 'SecondaryEnrichment',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Origin ID Type',
	@appValue = 'Isolate',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Origin ID Type*' ,
	@tableField = 'isolate.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 3 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Origin ID Type',
	@tranId = 62190 
		
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Page Ref' ,
	@tableField = 'isolate.notebookPage' ,
	@fieldOrdinal = 4 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex',
	@regexName = 'Notebook Ref', 
	@tranId = 62190 
	
/** -
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Vessel Type',
	@appValue = '96-well 2mL',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Vessel Type',
	@appValue = '15 ml tube',
	@tranId = 62190,
	@appValueId = @appvalueid output

exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Vessel Type',
	@appValue = '50 ml shake tube',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Vessel Type',
	@appValue = '25 sq cm flask',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Vessel Type',
	@appValue = '75 sq cm flask',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Vessel Type' ,
	@tableField = 'isolate.vesselType' ,
	@fieldOrdinal = 5 ,
	@optional = 1,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Isolates Vessel Type',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Isolation Method' ,
	@tableField = 'isolate.isolationMethod' ,
	@fieldOrdinal = 6 ,
	@optional = 1,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Isolation Method',
	@tranId = 62190 
	
/** 
	alter table isolate
	alter column dateArchived datetime null
**/
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Date Archived' ,
	@tableField = 'isolate.dateArchived' ,
	@fieldOrdinal = 7 ,
	@optional = 1,
	@active = 1 ,
	@validateAsDataType = 'datetime' ,
	@tranId = 62190
	
/** -
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Archive Method',
	@appValue = '5% DMSO',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Isolates Archive Method',
	@appValue = '10% Glycerol',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/
	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Archive Method' ,
	@tableField = 'isolate.archiveMethod' ,
	@fieldOrdinal = 8 ,
	@optional = 1,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Isolates Archive Method',
	@tranId = 62190 
	
	
	
/** -
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template Regex',
	@appValueType = 'Growth Medium',
	@appValue = '^PM\d{1,2}',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Growth Medium' ,
	@tableField = 'isolate.growthMedium' ,
	@fieldOrdinal = 9 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(50)' , -- per Gabe email 7/13/2011, not a regex
	@tranId = 62190 
	
/**
alter table isolate
alter column growthTemperature_C numeric(4,1) null

alter table isolate
alter column growthIrradiance numeric(6,3) null
**/

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Growth Temperature' ,
	@tableField = 'isolate.growthTemperature_C' ,
	@fieldOrdinal = 10 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(4,1)' , 
	@tranId = 62190 

	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Growth Irradiance' ,
	@tableField = 'isolate.growthIrradiance' ,
	@fieldOrdinal = 11 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(6,3)' , 
	@tranId = 62190 
	
/** -

dbcc opentran


delete appvalue where appvaluetype = 'Isolate Location'

declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template Regex',
	@appValueType = 'Isolate Location',
	@appValue = '1240:Rack\d{2}:\d{2}',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/

/** 
alter table isolate
alter column numericCoord smallint null
**/

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Location' ,
	@tableField = 'sampleLocation' , -- keep sync'd with spEMRE_bulkInsertIsolates
	@fieldOrdinal = 12 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex' ,
	@regexName = 'Isolate Location',
	@tranId = 62190 
		
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = 'Comments' ,
	@tableField = 'isolate.comment' ,
	@fieldOrdinal = 13,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(max)' , 
	@tranId = 62190 
	
/*****************************/
/*** 16S and 18S ANALYSIS ****/
begin tran

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '16S sequence' ,
	@tableField = 'isolate.sequence_16s' ,
	@fieldOrdinal = 14,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex' , 
	@regexName = 'RNA Sequence',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '16S database match' ,
	@tableField = 'isolate.databaseMatch_16S' ,
	@fieldOrdinal = 15,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(256)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '16S database match score' ,
	@tableField = 'isolate.datatbaseMatchScore_16s' ,
	@fieldOrdinal = 16,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(4,3)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '16S hierarchy' ,
	@tableField = 'isolate.hierarchy_16s' ,
	@fieldOrdinal = 17,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(500)' , 
	@tranId = 62190 
	
	/** 18S **/
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '18S sequence' ,
	@tableField = 'isolate.sequence_18s' ,
	@fieldOrdinal = 18,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(max)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '18S database match' ,
	@tableField = 'isolate.databaseMatch_18S' ,
	@fieldOrdinal = 19,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(256)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '18S database match score metrics' ,
	@tableField = 'isolate.databaseMatchScoreMetrics_18S' ,
	@fieldOrdinal = 20,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(500)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '18S hierarchy' ,
	@tableField = 'isolate.hierarchy_18s' ,
	@fieldOrdinal = 21,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(500)' , 
	@tranId = 62190 
	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'IsolatesImportFile',
	@worksheetName = 'Isolates' ,
	@templateField = '18S database match % ID' ,
	@tableField = 'isolate.databaseMatchPercentID_18S' ,
	@fieldOrdinal = 22,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,2)' , 
	@tranId = 62190 


-- MUST DO ONE OR THE OTHER:
-- commit
-- rollback
