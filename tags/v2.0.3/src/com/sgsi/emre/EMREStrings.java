package com.sgsi.emre;

import com.project.Strings;

/**
 * MetStrings
 *
 * Constants for Met project, e.g. itemTypes.
 * 
 * @author TJ Stevens/Wildtype Informatics LLC
 * @created 10/2007
 */
public class EMREStrings extends Strings
{
	/**
	 * These are DOM elements like "item","data","displayItems"...
	 * @author bobbyjo
	 *
	 */
	public static class DOMElement
	{
		
		public static final String	DISPLAYITEM 	= "displayItem";
		public static final String	DISPLAYITEMS 	= "displayItems";
		public static final String	SERVER 			= "server";
		public static final String	ITEMS 			= "items";
		public static final String	ITEM 			= "item";
		public static final String	DATA 			= "data";
		public static final String	VALUE 			= "value";
		public static final String  ADDASAPPFILE 	= "addAsAppFile";
	}
	
	/**
	 * DOM attributes like "itemType", "new", "optional"
	 * @author bobbyjo
	 *
	 */
	public static class DOMAttribute
	{
		public static final String	ITEMTYPE 		= "itemType";
		public static final String	TYPE 			= "type";
		public static final String	NEW 			= "new";
		public static final String	OPTIONAL 		= "optional";
		public static final String	COPYFILETO 		= "copyFileTo";
		public static final String	FILE 			= "file";
		public static final String	CONTAINER 		= "container";
		public static final String	APPLIESTO 		= "appliesTo";
		public static final String	CONTENTTYPE 	= "contentType";
		
	}
	
	/**
	 * Item types (primary containers that can have contents
	 * and be contents).
	 */
  public static class ItemType
	{
		// Primary item types
		public static final String SAMPLE					= "Sample";
		public static final String LIBRARY					= "Library";
		public static final String PROJECT					= "Project";
		public static final String CONTRIBUTOR				= "Contributor";
		public static final String DNA						= "DNA";
		public static final String RNA						= "RNA";
		public static final String CULTURE					= "Culture";
		public static final String EXPERIMENTALCULTURE		= "ExperimentalCulture";
		public static final String POOLED_CULTURES			= "PooledCultures";
		public static final String SHEARING_METHOD			= "ShearingMethod";
		public static final String CLONE					= "Clone";
		public static final String STRAIN					= "Strain";
		public static final String STRAINCULTURE			= "StrainCulture";
		public static final String AXENICSTRAIN				= "SecondaryIsolate";
		public static final String GENE						= "Gene";
		public static final String CLONE_CONSTRUCT			= "CloneConstruct";
		public static final String FRAGMENT					= "Fragment";
		public static final String PROTOCOL					= "Protocol";
		public static final String VESSEL					= "Vessel";
		public static final String INSERT					= "Insert";
		public static final String ADAPTOR_LIGATED_INSERT	= "AdaptorLigatedInsert";
		public static final String VECTOR					= "Vector";
		public static final String ACHEM_SUBMISSION			= "AChemSubmission";
		public static final String ACHEM_REQUEST			= "AChemRequest";
		public static final String MEDIA					= "Media";
		public static final String MEDIUM					= "Medium";
		public static final String EFT						= "EFT";
		public static final String FERMENTATION_BATCH		= "FermentationBatch";
		public static final String REACTOR_NUMBER			= "ReactorNumber";
		public static final String FERMENTATIONID			= "FermentationID";
		public static final String SUPPLEMENT1				= "Supplement1";
		public static final String CONCENTRATION1			= "Concentration1";
		public static final String UNITS1					= "Units1";
		public static final String SUPPLEMENT2				= "Supplement2";
		public static final String CONCENTRATION2			= "Concentration2";
		public static final String UNITS2					= "Units2";
		public static final String SUPPLEMENT3				= "Supplement3";
		public static final String CONCENTRATION3			= "Concentration3";
		public static final String UNITS3					= "Units3";
		public static final String TEMPERATURE				= "Temperature";
		public static final String pH						= "pH";
		public static final String DISSOLVED_O2				= "DissolvedO2";
		public static final String INITIAL_AGITATION		= "InitialAgitation";
		public static final String AIRFLOW_RATE				= "AirflowRate";
		public static final String AIRFLOW_RATE_UNITS		= "AirflowRateUnits";
		public static final String INITIAL_VOLUME			= "InitialVolume";
		public static final String INITIAL_VOLUME_UNITS		= "InitialVolumeUnits";
		public static final String FEED_RATE				= "FeedRate";
		public static final String FEED_RATE_UNITS			= "FeedRateUnits";
		public static final String FEED_MEDIUM				= "FeedMedium";
		public static final String COMMENTS					= "Comments";
		public static final String SOURCE_SAMPLE			= "SourceSample";
		public static final String BIOCHEM_ASSAY			= "BiochemAssay";
		public static final String BIOCHEM_ISOLATE			= "BiochemIsolate";
		public static final String BIOCHEM_SEQ_ISOLATE		= "BiochemSEQIsolate";
		public static final String BIOCHEM_ANA_ISOLATE		= "BiochemANAIsolate";
		
		public static final String ISOLATION_METHOD			= "IsolationMethod";
		public static final String ENVIRONMENTAL_SAMPLE		= "EnvironmentalSample";
		public static final String PRIMARY_ENRICHMENT		= "PrimaryEnrichment";
		public static final String SECONDARY_ENRICHMENT		= "SecondaryEnrichment";
		public static final String ISOLATE					= "Isolate";
		public static final String PLASMID					= "Plasmid";
		public static final String ORIGIN_LIMS_ID			= "OriginLIMSID";
		public static final String LIMS_ID					= "LIMSID";
		public static final String SAMPLING_ID				= "SamplingID";
		public static final String IMPORT_ID				= "ImportID";
	}
  
  public static class SGITask
	{
	  public static final String MEDIA_SETUP					= "Media Setup";
	  public static final String FERMENTATION_BATCH_SETUP		= "Fermentation Batch Setup";
	  public static final String REACTOR_SETUP					= "Reactor Setup";
	  public static final String MONITOR_BATCH					= "Monitor Batch";
	  public static final String CREATE_SAMPLE_SHEET			= "Create Sample Sheet";
	}
  
  /**
	 * File item types in Met workflow; may or may not be primary
	 * items.
	 * 
	 * 
	 * @author TJS/Wildtype for SGSI
	 * @date 4/2008
	 */
  public static class FileType
  {
	public static final String ACHEM_SUBMISSION						= "Analytical Chemistry Submission";
	public static final String PRIMARYENRICHMENT_SUBMISSION			= "Primary Enrichment";
	public static final String UPDATEPRIMARYENRICHMENT_SUBMISSION	= "Update Primary Enrichment";
	public static final String SECONDARYENRICHMENT_SUBMISSION 		= "Secondary Enrichment";
	public static final String UDPATESECONDARYENRICHMENT_SUBMISSION = "Update Secondary Enrichment";
	public static final String ISOLATES_SUBMISSION 					= "Isolates";
	
	
    public static final String SAMPLE_MANIFEST_FILE 		= "SampleImportFile";
    public static final String STRAIN_IMPORT_FILE 			= "StrainImportFile";
    public static final String EXTRACTION_IMPORT_FILE 		= "ExtractionImportFile";
    public static final String QUANT_IMPORT_FILE 			= "QuantImportFile";
    public static final String CULTIVATION_IMPORT_FILE 		= "CultivationImportFile";
    public static final String ARCHIVE_CULTURE_IMPORT_FILE 	= "ArchiveCultureImportFile";
    public static final String GROWTH_IMPORT_FILE 			= "GrowthImportFile";
    public static final String ISOLATION_IMPORT_FILE 		= "IsolationImportFile";
    public static final String PCR_IMPORT_FILE 				= "PCRImportFile";
    public static final String DEPLETION_IMPORT_FILE 		= "DepletionImportFile";
    public static final String HOLD_IMPORT_FILE 			= "HoldImportFile";
    public static final String REACTIVATE_IMPORT_FILE 		= "ReactivateImportFile";
    public static final String RETIREMENT_IMPORT_FILE 		= "RetirementImportFile";
    public static final String INSERT_IMPORT_FILE 			= "InsertImportFile";
    public static final String LIGATE_ADAPTORS_IMPORT_FILE 	= "LigateAdaptorsImportFile";
    public static final String LIGATE_INSERT_IMPORT_FILE 	= "LigateInsertImportFile";
    public static final String LIBRARY_QC_IMPORT_FILE 		= "LibraryQCImportFile";
    public static final String ACHEM_REQUEST_FILE 			= "AChemRequestFile";
    public static final String ANALYSIS_DATA_FILE 			= "AChemDataFile";
    public static final String VALIDATION_IMPORT_FILE		= "ValidationImportFile";
    public static final String TRANSFORMATION_IMPORT_FILE	= "TransformationImportFile";
    public static final String FOSMID_PACKAGING_IMPORT_FILE	= "FosmidPackagingImportFile";
    public static final String FOSMID_INFECTION_IMPORT_FILE	= "FosmidInfectionImportFile";
    public static final String COMBINE_SAMPLES_IMPORT_FILE	= "CombineSamplesImportFile";
    public static final String AMPLIFY_DNA_IMPORT_FILE		= "AmplifyDNAImportFile";
    public static final String SEQUENCING_IMPORT_FILE		= "SequencingImportFile";
    public static final String BIOCHEM_RESULTS_IMPORT_FILE				= "BiochemResultsImportFile";
    public static final String BIOCHEM_SEQUENCING_IMPORT_FILE 			= "BiochemSequencingImportFile";
    public static final String BIOCHEM_ANALYSIS_IMPORT_FILE 			= "BiochemAnalysisImportFile";
    public static final String POOL_CULTURES_IMPORT_FILE				= "PoolCulturesImportFile";
    public static final String PRIMARY_ENRICHMENT_IMPORT_FILE			= "PrimaryEnrichmentImportFile";
    public static final String UDPATE_PRIMARY_ENRICHMENT_IMPORT_FILE	= "UpdatePrimaryEnrichmentImportFile";
    public static final String SECONDARY_ENRICHMENT_IMPORT_FILE			= "SecondaryEnrichmentImportFile";
    public static final String UPDATE_SECONDARY_ENRICHMENT_IMPORT_FILE	= "UpdateSecondaryEnrichmentImportFile";
    public static final String ISOLATES_IMPORT_FILE						= "IsolatesImportFile";
    public static final String CULTURE_COLLECTION_IMPORT_FILE			= "CultureCollectionImportFile";
    public static final String SAMPLING_TIMEPOINT_IMPORT_FILE			= "SamplingTimepointImportFile";
    public static final String SAMPLING_DATA_IMPORT_FILE			= "CultureGrabDataImportFile";
  
  }
  
  /**
   * Delimiters and other character constants used in the Met workflow.
   */
  public static class CHAR
  {
	  public static final char SEMI_COLON 	= ';';
	  public static final String CRLF 		= "\r\n";
	  public static final char COMMA 		= ',';
  }
  
  /**
   * Analytical Chemistry lab task constants.
   */
  public static class AChem
  {
  	public static final String[] requiredFileHeaders = new String[]{"Request ID",
	"Request date","Requester","Sample description","Project","LIMS Tracking","Notebook Page",
	"Results Email Address","Comments","Acquisition date","Samples prepared by",
	"Calibration standards by","Data processed by","Analysis Type","Instrument","Method",
	"Units","Calibration curve range"};
  	
  	public static final String[] requiredDataHeaders = new String[]{"Request ID","Request date",
  		"Requester","Analysis method","Sample description","Project","LIMS Tracking",
  		"Results Email Address"};

	public static final String[] requiredSubmittedColumnHeaders = new String[]{"Submission ID",
		"LIMS ID Type","LIMS ID","Sampling Timepoint","Dilution","Comment"};
	
	public static final String[] requiredLabColumnHeaders = new String[]{"Submission ID",
	"Lab sample number","Lab sample ID"};
  }
  
  /**
   * GDSI lab task constants.
   */
  public static class Enrichments
  {
  	
  	public static final String[] requiredPrimaryColumnHeaders = new String[]{
  		"New LIMS ID","Origin LIMS ID","Page Ref ","Date Started ",
  		"Vessel Type","Growth Temperature","Growth Medium","Growth Irradiance",
  		"Internal ID","Comments"};
  	
  	public static final String[] requiredUpdatePrimaryColumnHeaders = new String[]{
  		"LIMS ID","Internal ID","Enrichment Result","Comments"};

	public static final String[] requiredSecondaryColumnHeaders = new String[]{
		"New LIMS ID","Origin LIMS ID","Origin Item Type","Page Ref","Growth Medium","O2 Concentration",
		"CO2 Concentration","pH","Flocculence","Comments"};
	
	public static final String[] requiredUpdateSecondaryColumnHeaders = new String[]{
  		"LIMS ID","O2 Concentration","CO2 Concentration","pH","Enrichment Result",
  		"Flocculence","Comments"};
	
	public static final String[] requiredIsolateColumnHeaders = new String[]{
		"New LIMS ID","Origin LIMS ID","Origin Item Type","Page Ref","Vessel Type","Isolation Method",
		"Date Archived","Archive Method","Growth Medium","Growth Temperature",
		"Growth Irradiance","Location","Comments"};
	
  }
  
  public static class GrowthRecovery
  {
	  public static final String[] requiredStrainCultureCollectionColumnHeaders = new String[]{
	  		"Strain ID","Start Date","Strain Culture ID","Notebook Page","Culture Description","Comments"};
	  
	  public static final String[] requiredExpCultureCollectionColumnHeaders = new String[]{
		  "Origin LIMS ID","Start Date","Experimental Culture ID","Notebook Page","Culture Description","Comments"};
	  
	  public static final String[] requiredUpdateTimepointsColumnHeaders = new String[]{
	  		"Culture ID","Sampling Timepoint"};
	  
	  public static final String[] requiredSamplingDataColumnHeaders = new String[] {
		  "Sampling ID", "Culture ID", "Sampling timepoint"};
  }
  
  public static class GeneDiscovery
  {
	  public static final String[] itemTypes = new String[]{"EnvironmentalSample",
		  "PrimaryEnrichment","SecondaryEnrichment","Isolate"};
	  
	  public static final String[] prefix = new String[]{"SI1","SI2","SI3","SI4"};
  }
}
