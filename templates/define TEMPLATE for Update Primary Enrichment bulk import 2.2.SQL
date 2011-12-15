/** 
	Define new bulk import template for appvaluetype = PrimaryEnrichmentImportFile
    Worksheet name = 'Update Primary Enrichment'
   @created 7/2011 by TJS/Wildtype for SGI
 **/
begin tran
rollback

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Update Primary Enrichment' ,
	@templateField = 'LIMS ID*' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Update Primary Enrichment' ,
	@templateField = 'Internal ID' ,
	@tableField = 'enrichment.internalId' ,
	@fieldOrdinal = 2 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(50)' , 
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Update Primary Enrichment' ,
	@templateField = 'Date of Result' ,
	@tableField = 'enrichment.dateOfResult' ,
	@fieldOrdinal = 3,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'datetime' , 
	@tranId = 62190 
	
declare @appValueId bigint
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Enrichment Result',
	@appValue = 'No Growth',
	@tranId = 62190,
	@appValueId = @appValueId
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Enrichment Result',
	@appValue = 'Growth',
	@tranId = 62190,
	@appValueId = @appValueId
	
exec dbo.spLinx_InsertAppValue
	@parentType = 'Template List',
	@appValueType = 'Enrichment Result',
	@appValue = 'Vigorous Growth',
	@tranId = 62190,
	@appValueId = @appValueId
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Update Primary Enrichment' ,
	@templateField = 'Enrichment Result' ,
	@tableField = 'enrichment.enrichmentResult' ,
	@fieldOrdinal = 4,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Enrichment Result',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'PrimaryEnrichmentImportFile',
	@worksheetName = 'Update Primary Enrichment' ,
	@templateField = 'Comments' ,
	@tableField = 'enrichment.comment' ,
	@fieldOrdinal = 5 ,
	@optional = 1 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(max)' , 
	@tranId = 62190 
	
-- MUST DO ONE OR THE OTHER:
-- commit
-- rollback
