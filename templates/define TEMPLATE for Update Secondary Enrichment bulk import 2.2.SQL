/** 
	Define new bulk import template for appvaluetype = SecondaryEnrichmentImportFile
    Worksheet name = 'Update Secondary Enrichment'
   @created 7/2011 by TJS/Wildtype for SGI
 **/
begin tran
rollback


exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'LIMS ID*' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,  /********/
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Origin LIMS ID' ,
	@tableField = 'enrichment.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 2 ,
	@optional = 1,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Origin ID Type' ,
	@tableField = 'origin' ,
	@fieldOrdinal = 3 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)',
	@tranId = 62190 

	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Date of Result' ,
	@tableField = 'enrichment.dateOfResult' ,
	@fieldOrdinal = 4,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'datetime' , 
	@tranId = 62190 
	
--delete templatedefinition where worksheetname = 'Update Secondary Enrichment' and fieldOrdinal = 9


	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Enrichment Result' ,
	@tableField = 'enrichment.enrichmentResult' ,
	@fieldOrdinal = 5,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Enrichment Result',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'O2 Concentration' ,
	@tableField = 'enrichment.oxygenConcentration' ,
	@fieldOrdinal = 6 ,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'CO2 Concentration' ,
	@tableField = 'enrichment.CO2Concentration' ,
	@fieldOrdinal = 7,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'pH' ,
	@tableField = 'enrichment.pH' ,
	@fieldOrdinal = 8,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Flocculence' ,
	@tableField = 'enrichment.flocculence' ,
	@fieldOrdinal = 9,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'list' , 
	@listName = 'Flocculence',
	@tranId = 62190 
	
/** -

alter table enrichment 
	add growthForm varchar(50) null
	
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Growth Form',
	@appValue = 'benthic',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Growth Form',
	@appValue = 'planktonic',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Growth Form',
	@appValue = 'mixed',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Growth Form' ,
	@tableField = 'enrichment.growthForm' ,
	@fieldOrdinal = 10,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Growth Form',
	@tranId = 62190 
	
/**
alter table enrichment
	add isolationAttempted bit null
alter table enrichment	
	add dateOfResult datetime null
	
declare @appvalueId bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template Regex',
	@appValueType = 'Y or N',
	@appValue = '^[YN]$',
	@tranId = 62190,
	@appValueId = @appvalueId output
**/
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Isolation Attempted?' ,
	@tableField = 'enrichment.isolationAttempted' ,
	@fieldOrdinal = 11 ,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex' , 
	@regexName = 'Y or N',
	@tranId = 62190 
	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Update Secondary Enrichment' ,
	@templateField = 'Comments' ,
	@tableField = 'enrichment.comment' ,
	@fieldOrdinal = 12 ,/********/
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(max)' , 
	@tranId = 62190 
	
-- MUST DO ONE OR THE OTHER:
-- commit
-- rollback
