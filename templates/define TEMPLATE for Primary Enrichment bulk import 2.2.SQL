/** 
	Define new bulk import template for appvaluetype = PrimaryEnrichmentImportFile
  
   @created 7/2011 by TJS/Wildtype for SGI
 **/
begin tran

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'New LIMS ID*' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Origin LIMS ID*' ,
	@tableField = 'enrichment.originItemId' , -- requires custom handling in sp
	@fieldOrdinal = 2 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Page Ref' ,
	@tableField = 'enrichment.notebookPage' ,
	@fieldOrdinal = 3 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'regex',
	@regexName = 'Notebook Ref', -- turn on when feature is ready
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Date Started' ,
	@tableField = 'enrichment.dateStarted' ,
	@fieldOrdinal = 4 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'datetime' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Vessel Type' ,
	@tableField = 'enrichment.vesselType' ,
	@fieldOrdinal = 5 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(50)',
	--@validateAsDataType = 'list' ,   -- turn on when feature is ready
	--@listName = 'Primary Enrichment Vessel Type Template List',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Growth Temperature' ,
	@tableField = 'enrichment.growthTemperature_C' ,
	@fieldOrdinal = 6 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(6,2)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Growth Medium' ,
	@tableField = 'enrichment.growthMedium' ,
	@fieldOrdinal = 7 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(50)' , -- should this be regex PMB_?
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Growth Irradiance' ,
	@tableField = 'enrichment.growthIrradiance' ,
	@fieldOrdinal = 8 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(6,3)' , 
	@tranId = 62190 
	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'CO2 Concentration' ,
	@tableField = 'enrichment.CO2Concentration' ,
	@fieldOrdinal = 9 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'numeric(3,1)' , 
	@tranId = 62190 	

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
	@templateField = 'Internal ID' ,
	@tableField = 'enrichment.internalId' ,
	@fieldOrdinal = 10 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(50)' , 
	@tranId = 62190 	
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Primary Enrichment' ,
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
