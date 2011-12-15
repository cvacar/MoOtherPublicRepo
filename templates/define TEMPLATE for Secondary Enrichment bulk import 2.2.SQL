/** 
	Define new bulk import template for appvaluetype = SecondaryEnrichmentImportFile
  
   @created 7/2011 by TJS/Wildtype for SGI
 **/
 
-- select * from vwLinx_AppValues where appvaluetype like '%Secondary%'
 
begin tran

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'New LIMS ID*' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Origin LIMS ID*' ,
	@tableField = 'enrichment.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 2 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Origin ID Type*' ,
	@tableField = 'enrichment.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 3 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
		
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Page Ref' ,
	@tableField = 'enrichment.notebookPage' ,
	@fieldOrdinal = 4 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex',
	@regexName = 'Notebook Ref', 
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
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Growth Medium' ,
	@tableField = 'enrichment.growthMedium' ,
	@fieldOrdinal = 5 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'regex' , 
	@regexName = 'Growth Medium',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'O2 Concentration' ,
	@tableField = 'enrichment.oxygenConcentration' ,
	@fieldOrdinal = 6 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(5,1)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'CO2 Concentration' ,
	@tableField = 'enrichment.CO2Concentration' ,
	@fieldOrdinal = 7 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Temperature deg C' ,
	@tableField = 'enrichment.growthTemperature_C' ,
	@fieldOrdinal = 8 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190 
	

	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'pH' ,
	@tableField = 'enrichment.pH' ,
	@fieldOrdinal = 9 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(6,3)' , 
	@tranId = 62190 
	
/** -
declare @appvalueid bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Flocculence',
	@appValue = 'None',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Flocculence',
	@appValue = 'Minor',
	@tranId = 62190,
	@appValueId = @appvalueid output
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Flocculence',
	@appValue = 'Major',
	@tranId = 62190,
	@appValueId = @appvalueid output
**/
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Flocculence' ,
	@tableField = 'enrichment.flocculence' ,
	@fieldOrdinal = 10 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'list' , 
	@regexName = 'Flocculence',
	@tranId = 62190 	
	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'SecondaryEnrichmentImportFile',
	@worksheetName = 'Secondary Enrichment' ,
	@templateField = 'Comments' ,
	@tableField = 'enrichment.comment' ,
	@fieldOrdinal = 11 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(max)' , 
	@tranId = 62190 
	
-- MUST DO ONE OR THE OTHER:
-- commit
-- rollback
