/*** 
	Define bulk import template for appvaluetype = 'Sample logging bulk import'
	Eff EMRE LIMS v2.2
	@created 7/2011 by TJS/Wildtype for SGI
*/

begin tran

exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'Sample Logging bulk import',
	@worksheetName = 'Aquatic' ,
	@templateField = 'LIMS ID' ,
	@tableField = 'ITEM.item' ,
	@fieldOrdinal = 1 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'varchar(100)' ,
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'Sample Logging bulk import',
	@worksheetName = 'Aquatic' ,
	@templateField = 'Physical Form' ,
	@tableField = 'sample.physicalForm' ,
	@fieldOrdinal = 2 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'list' ,
	@listName = 'Physical Form',
	@tranId = 62190 
	
exec spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'Sample Logging bulk import',
	@worksheetName = 'Aquatic' ,
	@templateField = 'Notebook Ref' ,
	@tableField = 'sample.notebookReference' ,
	@fieldOrdinal = 4 ,
	@optional = 0 ,
	@active = 1 ,
	@validateAsDataType = 'regex' ,
	@regexName = 'Notebook Ref',
	@tranId = 62190 
	
-- commit
-- rollback

dbcc OPENTRAN