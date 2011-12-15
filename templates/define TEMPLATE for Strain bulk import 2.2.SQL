/***********************************
	Define bulk import template 
	for EMRE LIMS v2.2 task Update Strain.
	Worksheet = Strains - Euk & Cyano
	and
	Worksheet = Strains - Prok
	
	@created 7/2011 by TJS/Wildtype for SGI
	
****************************************/
begin tran 

rollback
commit


exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Strain ID',
	@tableField = 'ITEM.item',
	@fieldOrdinal = 1,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(200)',
	@tranId = 62190
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Strain Name',
	@tableField = 'strain.strainName',
	@fieldOrdinal = 2,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(200)',
	@tranId = 62190	
	
	/**
	declare @appvalueid bigint
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'WE',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'NE',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'GE',
		@tranId = 62190,
		@appValueId = @appvalueid output

	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'WC',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'NC',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Eukaryote',
		@appValue = 'GC',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/

exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Strain Type',
	@tableField = 'strain.strainType',
	@fieldOrdinal = 3,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(200)',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Genus',
	@tableField = 'strainFeature',
	@fieldOrdinal = 4,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Species',
	@tableField = 'strainFeature',
	@fieldOrdinal = 5,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	
	
	/**
	declare @appvalueid bigint
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Origin ID - Eukaryote',
		@appValue = '(WT|PH|WC|NC|GC|WE|NE|GE)-SGI-E-[0-9]{4,5}',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Origin ID - Eukaryote',
		@appValue = 'SI4-SGI-E-[0-9]{6}-[ABCDEFGH][0-9]{2}',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Origin ID',
	@tableField = 'strain.originItemId',
	@fieldOrdinal = 6,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	
	
	/**
	declare @appvalueid bigint
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Projects',
		@appValue = 'EMRE',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Project',
	@tableField = 'strain.projectItemId',
	@fieldOrdinal = 7,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'list',
	@listName = 'Strain Projects',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Page Ref [notebook-pg]',
	@tableField = 'strain.notebookRef',
	@fieldOrdinal = 8,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Notebook Ref',
	@tranId = 62190	
	
	/**
	delete appvalue where appvaluetype = 'Strain Location' and appvalue like '%{1,2}'
	declare @appvalueid bigint
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Location',
		@appValue = '1241:SB\d{1,2}:\d{1,2}',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Location',
		@appValue = '1052:SBBU\d{1,2}:\d{1,2}',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Location 1 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 9,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Location 2 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 10,
	@optional = 1, -- optional per Jay's email 7/11
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Location 3 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 11,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Location 4 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 12,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Euk & Cyano',
	@templateField = 'Comment',
	@tableField = 'strain.comment',
	@fieldOrdinal = 13,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(max)',
	@tranId = 62190	
	
/********** PROKARYOTE WORKSHEET ************/
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Strain ID',
	@tableField = 'ITEM.item',
	@fieldOrdinal = 1,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(200)',
	@tranId = 62190
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Strain Name',
	@tableField = 'strain.strainName',
	@fieldOrdinal = 2,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(200)',
	@tranId = 62190	

	/**
	declare @appvalueid bigint
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Prokaryote',
		@appValue = 'WH',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Prokaryote',
		@appValue = 'NH',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_insertAppValue
		@parentType = 'Template List',
		@appValueType = 'Strain Type - Prokaryote',
		@appValue = 'GH',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/

exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Strain Type',
	@tableField = 'strain.strainType',
	@fieldOrdinal = 3,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'list',
	@listName = 'Strain Type - Prokaryote',
	@tranId = 62190	

exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Genus',
	@tableField = 'strainFeature',
	@fieldOrdinal = 4,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Species',
	@tableField = 'strainFeature',
	@fieldOrdinal = 5,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	

	/**
	declare @appvalueid bigint
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Origin ID - Prokaryote',
		@appValue = '(WT|PE|WH|NH|GH)-SGI-E-[0-9]{4,5}',
		@tranId = 62190,
		@appValueId = @appvalueid output
		
	exec dbo.spLinx_InsertAppValue
		@parentType = 'Template Regex',
		@appValueType = 'Strain Origin ID - Prokaryote',
		@appValue = 'SI4-SGI-E-[0-9]{6}-[ABCDEFGH][0-9]{2}',
		@tranId = 62190,
		@appValueId = @appvalueid output
	**/

exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Origin ID',
	@tableField = 'strain.originItemId',
	@fieldOrdinal = 6,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'varchar(250)',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Project',
	@tableField = 'strain.projectItemId',
	@fieldOrdinal = 7,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'list',
	@listName = 'Strain Projects',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Page Ref [notebook-pg]',
	@tableField = 'strain.notebookRef',
	@fieldOrdinal = 8,
	@optional = 0,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Notebook Ref',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Location 1 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 9,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Location 2 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 10,
	@optional = 1, -- optional per Jay's email 7/11
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	
	
	
exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Location 3 [Freezer:Box:Position]',
	@tableField = 'strainLocation',
	@fieldOrdinal = 11,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'regex',
	@regexName = 'Strain Location',
	@tranId = 62190	

exec dbo.spEMRE_insertOrUpdateTemplateDefinitionField
	@templateName = 'StrainImportFile',
	@worksheetName = 'Strains - Prok',
	@templateField = 'Comment',
	@tableField = 'strain.comment',
	@fieldOrdinal = 13,
	@optional = 1,
	@active = 1,
	@validateAsDataType = 'varchar(max)',
	@tranId = 62190	