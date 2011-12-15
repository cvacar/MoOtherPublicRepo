/** Script to check bulk import results **/
/** created 7/2011 TJS/Wildtype for Gabe **/
/** queries for EMREDbTest_2_2 on sqlemre01 **/


/*** PEEK TEMPLATES ***/

select worksheetName, fieldOrdinal, templateField, optional, dataFormat, 
	case 
		when listAppValueType is null or listAppValueType = 'N/A' then ''
		else listAppValueType end [listAppValueType],
	case
		when regexAppValueType is null or regexAppValueType = 'N/A' then ''
		else regexAppValueType end [regexAppvalueType],
		*
from templateDefinition td inner join templateFieldDefinition field
on td.templatefielddefinitionId = field.templateFieldDefinitionId
where td.worksheetName like 'Strains%'
order by td.worksheetName, td.fieldOrdinal

/** PEEK TEMPLATE LISTS **/

select * from vwLinx_AppValues with(NOLOCK)
where parenttype = 'Template List'
order by vwLinx_AppValues.appvaluetype, vwLinx_AppValues.appvalueid

/** PEEK TEMPLATE REGEX **/

select * from vwLinx_AppValues with(NOLOCK)
where parenttype = 'Template Regex'
order by vwLinx_AppValues.appvaluetype, vwLinx_AppValues.appvalueid

/*** PEEK AQUATIC SAMPLES **/

select top 100 vw.item, * 
from sample e with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on e.sampleItemId = vw.itemId
order by s.tranId desc

/*** PEEK ENRICHMENTS **/

select top 100 vw.item, * 
from enrichment e with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on e.enrichmentitemId = vw.itemId
order by e.tranId desc

/*** PEEK ISOLATES ***/

select top 100 vw.item, * 
from isolate i with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on i.isolateitemId = vw.itemId
order by i.tranId desc

/*** PEEK STRAINS ***/

select top 100 vw.item, * 
from strain s with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on vw.itemId = s.strainitemId
order by s.tranId desc

/*** PEEK STRAIN LOCATIONS ***/

select top 10 vw.item, * 
from strainLocation loc with(nolock) inner join VWLinx_Items vw with(NOLOCK) on vw.itemId = loc.strainitemId
order by loc.tranId desc

/*** PEEK STRAIN FEATURES ***/

select top 100 vw.item, dt.dataType, featureValue, s.tranId, * 
from strainFeature s with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on vw.itemId = s.strainitemId
inner join dataType dt with(nolock) on dt.dataTypeId = s.featureTypeId
order by s.tranId desc
--where s.tranid = 62635

/*** PEEK STRAIN TOLERANCES ***/

select distinct top 100 vw.item, dt.datatype, av.APPVALUE, s.tranId
from strainTolerance s with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on vw.itemId = s.strainitemId
inner join dataType dt with(nolock) on dt.dataTypeId = s.toleranceTypeId
inner join APPVALUE av with(nolock) on av.APPVALUEID = s.toleranceAppValueId
order by s.tranId desc, dt.datatype

/*** PEEK STRAIN HISTORY ***/

select top 100 vw.item, dt.dataType, featureValue, s.revisionTranId, * 
from strainFeatureHistory s with(NOLOCK) inner join VWLinx_Items vw with(NOLOCK) on vw.itemId = s.strainitemId
inner join dataType dt with(nolock) on dt.dataTypeId = s.featureTypeId
order by s.revisionTranId desc



select datatype from dataType
