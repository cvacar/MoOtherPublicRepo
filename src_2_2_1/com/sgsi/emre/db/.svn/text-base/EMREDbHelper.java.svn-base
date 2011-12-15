package com.sgsi.emre.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;

import com.sgsi.db.SGIDbHelper;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.util.PrintBarcode;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.model.TableDataMap;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.util.LimsTokenizer;
import com.wildtype.linx.util.LinxDbException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

/**
 * MetDbHelper
 * 
 * 
 * @author TJ Stevens/Wildtype for SGSI
 * @created 6/2007
 */
public class EMREDbHelper extends SGIDbHelper
{

	/**
	 * Returns sample type for the given sample.
	 * 
	 * @param sample
	 * @param db
	 * @return one of [ISO,ENR,ENV,DNA,TIS]
	 */
	public String getSampleTypeBySample(String sample, Db db)
	{
		String sql = "exec spMet_GetSampleTypeBySample '" + sample + "'";
		String sampleType = getDbValue(sql, db);
		return sampleType;
	}

	/**
	 * Returns a list of previously assigned Locations for the given Strain.
	 * Relies solely on core tables.
	 * 
	 * @param strain
	 * @param db
	 * @return list of locations for strain (may be empty)
	 */
	public List getLocationsForStrain(String strain, Db db)
	{
		String sql = "exec spMet_GetLocationsForStrain '" + strain + "'";
		List locs = getListEntries(sql, db);
		return locs;
	}

	/**
	 * Inserts a record into the workRequest table.
	 * 
	 * @param requestName
	 * @param requestType
	 * @param requester
	 * @param description
	 * @param comment
	 * @param tranId
	 * @param db
	 */
	public void insertWorkRequest(String requestName, String requestType,
			String requestDate, String requester, String description,
			String requestAppFileId, String comment, String project,
			String limsTracking, String notebookPage, String emailAddress,
			long tranId, Db db)
	{
		ArrayList<String> alParams = new ArrayList<String>();
		alParams.add(requestName);
		alParams.add(requestType);
		alParams.add(requestDate);
		alParams.add(requester);
		alParams.add(description);
		alParams.add(requestAppFileId);
		alParams.add(comment);
		alParams.add(project);
		alParams.add(limsTracking);
		alParams.add(notebookPage);
		alParams.add(emailAddress);
		alParams.add(tranId + "");
		String sp = "spMet_insertWorkRequest";
		callStoredProc(db, sp, alParams, false);

	}

	/**
	 * Updates a record into the workRequest table.
	 * 
	 * @param requestName
	 * @param requestType
	 * @param requester
	 * @param description
	 * @param comment
	 * @param tranId
	 * @param db
	 */
	public void updateEditedWorkRequest(String requestName, String requestDate,
			String requester, String description, String requestAppFileId,
			String comment, String project, String notebook, String email,
			long tranId, Db db)
	{
		ArrayList<String> alParams = new ArrayList<String>();
		alParams.add(requestName);
		alParams.add(requestDate);
		alParams.add(requester);
		alParams.add(description);
		alParams.add(requestAppFileId);
		alParams.add(comment);
		alParams.add(project);
		alParams.add(notebook);
		alParams.add(email);
		alParams.add(tranId + "");
		String sp = "spMet_updateEditedWorkRequest";
		callStoredProc(db, sp, alParams, false);

	}

	/**
	 * inserts a record into the comment table and returns the newly created
	 * primary key
	 * 
	 * @param commentType
	 * @param comment
	 * @param tranId
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public String insertComment(String commentType, String comment,
			long tranId, Db db) throws Exception
	{
		String rtn = null;
		try
		{
			ArrayList<String> alParams = new ArrayList<String>();
			alParams.add(commentType);
			alParams.add(comment);
			alParams.add(tranId + "");
			String sp = "spLinx_AppendComment";
			rtn = callStoredProc(db, sp, alParams, true);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return rtn;
	}

	public String insertSubmissionParameter(String submissionRequestId,
			String paramTypeId, String paramValue, int outputOrder,
			long tranId, Db db) throws Exception
	{
		String rtn = null;
		try
		{
			ArrayList<String> alParams = new ArrayList<String>();
			alParams.add(submissionRequestId);
			alParams.add(paramTypeId);
			alParams.add(paramValue);
			alParams.add(outputOrder + "");
			alParams.add(tranId + "");
			String sp = "spMet_insertSubmissionParameter";
			rtn = callStoredProc(db, sp, alParams, true);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return rtn;
	}

	/**
	 * returns the appValueId as a long for the given parenttype, appvaluetype,
	 * and appvalue if the variable bInsert is true, we insert the record into
	 * the db and return the id if the variable bThrowException is true we throw
	 * an exception if an appValueId is not returned
	 * 
	 * @param parentType
	 * @param appValueType
	 * @param appValue
	 * @param bInsert
	 * @param bThrowException
	 * @param task
	 * @param db
	 * @return
	 */
	public long getAppValueIdAsLong(String parentType, String appValueType,
			String appValue, boolean bInsert, boolean bThrowException,
			Task task, Db db)
	{
		long appValueId = -1;
		try
		{
			if (parentType == null || parentType.equals(""))
				throw new Exception("ParentType cannot be null.");
			if (appValueType == null || appValueType.equals(""))
				throw new Exception("AppValueType cannot be null.");
			String insert = "0";
			if (bInsert)
				insert = "1";
			String sp = "spMet_getAppValueId";
			ArrayList<String> params = new ArrayList<String>();
			params.add(parentType);
			params.add(appValueType);
			params.add(appValue);
			params.add(insert);
			params.add(task.getTranId() + "");

			String id = callStoredProc(db, sp, params, true, true);
			if (id == null)
			{
				if (bThrowException)
				{
					throw new Exception(
							"There is no appValueId for parentType "
									+ parentType + "; appValueType "
									+ appValueType + "; appValue " + appValue
									+ " in the database.");
				}
			}
			else
			{
				appValueId = Long.valueOf(id);
			}

		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred during retrieval of appValueId: "
							+ ex.getMessage());
		}
		return appValueId;
	}

	public String addApplicationFile(String parentPath, String filenameOnly,
			String fileTypeId, String appliesToItemId, long tranId, Db db)
	{
		// must look up parentDirectory.parent_directory_id
		String sql = "select parent_directory_id from PARENTDIRECTORY where path = '"
				+ parentPath + "'";
		String parentDirId = getDbValue(sql, db);
		if (WtUtils.isNullOrBlank(parentDirId))
		{
			// create new parent path
			ArrayList<String> params = new ArrayList<String>();
			params.add(parentPath);
			params.add(tranId + "");
			parentDirId = this.callStoredProc(db,
					"spLinx_InsertParentDirectoryOutput", params, true, true);
		}
		// by here, have parentDirectoryId for parent path

		// create new app file
		ArrayList<String> params = new ArrayList<String>();
		params.add(parentDirId);
		params.add(fileTypeId);
		params.add(filenameOnly);
		params.add(appliesToItemId);
		params.add(tranId + "");

		String id = this.callStoredProc(db,
				"spLinx_InsertApplicationFileForItemId", params, true, true);
		return id;

	}

	/**
	 * returns a PrintService for a given task
	 * 
	 * @param taskName
	 * @param db
	 * @return
	 */
	public PrintService getPrintServiceForTask(String taskName, Db db)
	{
		PrintService printService = null;
		try
		{
			String zebraPrinter = db.getHelper().getApplicationValue(db,
					"Zebra Printer", taskName);
			printService = PrintBarcode.getPrintService(zebraPrinter);
			if (printService == null)
			{
				throw new Exception(
						"Unable to locate the Zebra printer on the network."
								+ " Please alert your LIMS administrator.");
			}
		}
		catch (Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return printService;
	}

	/**
	 * inserts a record into the media table
	 * 
	 * @param itemId
	 * @param typeId
	 * @param fileId
	 * @param tranId
	 */
	public String insertMedia(String itemId, String typeId, String fileId,
			String parentMediaId, long tranId, Db db) throws Exception
	{
		String mediaId = null;
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(itemId);
			params.add(null);// mediumTypeId - not supported yet
			params.add(fileId);
			params.add(parentMediaId);
			params.add(tranId + "");

			String sql = "spMet_insertMedia ";
			mediaId = callStoredProc(db, sql, params, true, true);
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return mediaId;
	}

	/**
	 * retrieve the parent media id for the current selected medium in the table
	 * if there is no selected media in the table the parent media id will be
	 * null as we have a new meduim
	 * 
	 * @return
	 */
	public String getParentMediaId(String media, Db db)
	{
		String rtn = null;
		try
		{
			rtn = getDbValue("exec spMet_getMediaId '" + media + "'", db);

		}
		catch (Exception ex)
		{
			throw new LinxUserException("Unable to retrieve parent media: "
					+ ex.getMessage());
		}
		return rtn;
	}

	/**
	 * inserts the selected supplements into the mediaSupplement table
	 * 
	 * @param mediaId
	 * @param supplements
	 * @param tranid
	 */
	public void insertMediaSupplement(String mediaId, String supplements,
			long tranid, Task task, Db db)
	{
		try
		{
			// lets split the supplements on ;
			LimsTokenizer tk = new LimsTokenizer(supplements, ';');
			int numTokens = tk.getTokenCount();
			for (int i = 0; i < numTokens; i++)
			{
				String supp = tk.getTokenAt(i);
				// String suppId = getApplicationValueId(db, "Supplement",
				// supp);
				executeSQL("exec spMet_insertMediaSupplement " + mediaId
						+ ", '" + supp + "'," + tranid, db);
			}
		}
		catch (Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
	}

	/**
	 * inserts a record into the fermBatch table and returns the newly inserted
	 * primary key
	 * 
	 * @param batchItemId
	 * @param strainItemId
	 * @param numberReactors
	 * @param batchTypeId
	 * @param tranid
	 * @param db
	 * @return
	 */
	public long insertFermentationBatch(String batchItemId,
			String strainItemId, String numberReactors, String batchTypeId,
			long tranid, Db db)
	{
		long batchId = -1;
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(batchItemId);
			params.add(strainItemId);
			params.add(numberReactors);
			params.add(batchTypeId);
			params.add(tranid + "");
			String procName = "spMet_insertFermBatch";
			batchId = Long.valueOf(callStoredProc(db, procName, params, true));
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return batchId;
	}

	/**
	 * inserts a record into the fermBatchDefinition table
	 * 
	 * @param batchId
	 * @param mediaId
	 * @param supplement1Id
	 * @param supplement1Conc
	 * @param supplement1Units
	 * @param supplement2Id
	 * @param supplement2Conc
	 * @param supplement2Units
	 * @param supplement3Id
	 * @param supplement3Conc
	 * @param supplement3Units
	 * @param temperature
	 * @param pH
	 * @param dissolvedO2
	 * @param initialAgitation
	 * @param airflowRate
	 * @param airflowRateUnits
	 * @param initialVolume
	 * @param feedMedium
	 * @param flowRate
	 * @param flowRateUnits
	 * @param commentId
	 * @param tranid
	 * @param db
	 */
	public void insertFermentationBatchDefinition(String batchId,
			String mediaId, String supplement1Id, String supplement1Conc,
			String supplement1Units, String supplement2Id,
			String supplement2Conc, String supplement2Units,
			String supplement3Id, String supplement3Conc,
			String supplement3Units, String temperature, String pH,
			String dissolvedO2, String initialAgitation, String airflowRate,
			String airflowRateUnits, String initialVolume,
			String initialvolumeunits, String feedMedium, String flowRate,
			String flowRateUnits, String commentId, long tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(batchId);
			params.add(mediaId);
			params.add(supplement1Id);
			params.add(supplement1Conc);
			params.add(supplement1Units);
			params.add(supplement2Id);
			params.add(supplement2Conc);
			params.add(supplement2Units);
			params.add(supplement3Id);
			params.add(supplement3Conc);
			params.add(supplement3Units);
			params.add(temperature);
			params.add(pH);
			params.add(dissolvedO2);
			params.add(initialAgitation);
			params.add(airflowRate);
			params.add(airflowRateUnits);
			params.add(initialVolume);
			params.add(initialvolumeunits);
			params.add(feedMedium);
			params.add(flowRate);
			params.add(flowRateUnits);
			params.add(commentId);
			params.add(tranid + "");
			String procName = "spMet_insertFermBatchDefinition";
			callStoredProc(db, procName, params, false);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	public void insertFermentationBatchDetail(String mediaId,
			String reactorNumber, String fermDefId, String fermItemId,
			String strainItemId, String supplement1Id, String supplement1Conc,
			String supplement1Units, String supplement2Id,
			String supplement2Conc, String supplement2Units,
			String supplement3Id, String supplement3Conc,
			String supplement3Units, String temperature, String pH,
			String dissolvedO2, String initialAgitation, String airflowRate,
			String airflowRateUnits, String initialVolume,
			String initialvolumeunits, String feedMedium, String flowRate,
			String flowRateUnits, String commentId, long tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(mediaId);
			params.add(fermDefId);
			params.add(fermItemId);
			params.add(strainItemId);
			params.add(reactorNumber);
			params.add(supplement1Id);
			params.add(supplement1Conc);
			params.add(supplement1Units);
			params.add(supplement2Id);
			params.add(supplement2Conc);
			params.add(supplement2Units);
			params.add(supplement3Id);
			params.add(supplement3Conc);
			params.add(supplement3Units);
			params.add(temperature);
			params.add(pH);
			params.add(dissolvedO2);
			params.add(initialAgitation);
			params.add(airflowRate);
			params.add(airflowRateUnits);
			params.add(initialVolume);
			params.add(initialvolumeunits);
			params.add(feedMedium);
			params.add(flowRate);
			params.add(flowRateUnits);
			params.add(commentId);
			params.add(tranid + "");
			String procName = "spMet_insertFermBatchDetail";
			callStoredProc(db, procName, params, false);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	public void updateFermentationBatchDetail(String fermBatchDetailId,
			String mediaId, String reactorNumber, String fermDefId,
			String fermItemId, String strainItemId, String supplement1Id,
			String supplement1Conc, String supplement1Units,
			String supplement2Id, String supplement2Conc,
			String supplement2Units, String supplement3Id,
			String supplement3Conc, String supplement3Units,
			String temperature, String pH, String dissolvedO2,
			String initialAgitation, String airflowRate,
			String airflowRateUnits, String initialVolume,
			String initialvolumeunits, String feedMedium, String flowRate,
			String flowRateUnits, String commentId, long tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(fermBatchDetailId);
			params.add(mediaId);
			params.add(fermDefId);
			params.add(fermItemId);
			params.add(strainItemId);
			params.add(reactorNumber);
			params.add(supplement1Id);
			params.add(supplement1Conc);
			params.add(supplement1Units);
			params.add(supplement2Id);
			params.add(supplement2Conc);
			params.add(supplement2Units);
			params.add(supplement3Id);
			params.add(supplement3Conc);
			params.add(supplement3Units);
			params.add(temperature);
			params.add(pH);
			params.add(dissolvedO2);
			params.add(initialAgitation);
			params.add(airflowRate);
			params.add(airflowRateUnits);
			params.add(initialVolume);
			params.add(initialvolumeunits);
			params.add(feedMedium);
			params.add(flowRate);
			params.add(flowRateUnits);
			params.add(commentId);
			params.add(tranid + "");
			String procName = "spMet_updateFermBatchDetail";
			callStoredProc(db, procName, params, false);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	public String getFermentationBatchDetailId(String fermDefId,
			String fermItemId, Db db)
	{
		String id = null;
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(fermDefId);
			params.add(fermItemId);
			String procName = "spMet_getFermBatchDetailId";
			id = getDbValueFromStoredProc(db, procName, params);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return id;
	}

	/**
	 * returns the supplement id (appvalueid)
	 * 
	 * @param supplement
	 * @param task
	 * @param db
	 * @return
	 */
	public String getSupplementId(String supplement, Task task, Db db)
	{
		String rtn = null;
		if (!WtUtils.isNullOrBlank(supplement)
				&& !supplement.equalsIgnoreCase("(Select)"))
			rtn = String.valueOf(getAppValueIdAsLong("Supplement", supplement,
					null, false, false, task, db));
		return rtn;
	}

	/**
	 * retrieves the commentId from the comment table
	 * 
	 * @param comment
	 * @param db
	 * @return
	 */
	public long getCommentId(String comment, Db db)
	{
		long id = -1;
		try
		{
			String sId = getDbValue("spMet_getCommentId '" + comment + "'", db);
			if (!WtUtils.isNullOrBlank(sId))
				id = Long.valueOf(sId);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return id;
	}

	/**
	 * retrieves active fermentation batches from the database
	 * 
	 * @param db
	 * @return
	 */
	public ArrayList<String> getBatchArray(Task task, Db db)
	{
		ArrayList<String> rtn = null;
		try
		{
			rtn = getListEntries("exec spMet_getAvailableFermBatches '"
					+ task.getTaskName() + "'", db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred in retrieving batches from the database: "
							+ ex.getMessage());
		}
		return rtn;
	}

	/**
	 * returns the 10 most recently queued batches
	 * 
	 * @param task
	 * @param db
	 * @return
	 */
	public ArrayList<String> getRecentTenBatches(Task task, Db db)
	{
		ArrayList<String> rtn = null;
		try
		{
			rtn = getListEntries("exec spMet_getLatestTenBatches '"
					+ task.getTaskName() + "'", db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred in retrieving batches from the database: "
							+ ex.getMessage());
		}
		return rtn;
	}

	public ArrayList<String> getAnalysisMethods(Db db)
	{
		ArrayList<String> rtn = null;
		try
		{
			rtn = getListEntries("exec spMet_getAnalysisMethods ", db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred in retrieving analysis methods from the database: "
							+ ex.getMessage());
		}
		return rtn;
	}

	public ArrayList<String> getRequestTypes(Db db)
	{
		ArrayList<String> rtn = null;
		try
		{
			rtn = getListEntries("exec spMet_getAnalysisDataTypes ", db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred in retrieving request types from the database: "
							+ ex.getMessage());
		}
		return rtn;
	}

	/**
	 * dequeues an item from all queues
	 * 
	 * @param item
	 * @param itemType
	 * @param db
	 */
	public void dequeueAll(String item, String itemType, Db db)
			throws Exception
	{
		this.executeSQL("exec spMet_dequeueAll '" + item + "','" + itemType
				+ "'", db);

	}

	public void addItemComment(String comment, String commentType, String item,
			String itemType, long tranid, Db db) throws Exception
	{
		long commentId = 0;
		this.executeSQL("exec spLinx_InsertCommentForItem '" + item + "','"
				+ itemType + "','" + comment + "','" + commentType + "',"
				+ tranid + "," + commentId, db);
	}

	/**
	 * inserts a record into the biochemData table
	 * 
	 * @param assayId
	 * @param biochemIsolateId
	 * @param assayName
	 * @param value
	 * @param tranid
	 * @param db
	 */
	public void insertBiochemData(String assayId, String biochemIsolateId,
			String assayName, String value, String tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(assayId);
			params.add(biochemIsolateId);
			params.add(assayName);
			params.add(value);
			params.add(tranid);
			String sql = "spMet_insertBiochemData";
			callStoredProc(db, sql, params, true);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred while inserting assay data: "
							+ ex.getMessage());
		}
	}

	/**
	 * inserts a record into the biochemLocation table
	 * 
	 * @param box
	 * @param position
	 * @param isolateId
	 * @param tranid
	 * @param db
	 */
	public void insertBiochemLocation(String box, String position,
			String isolateId, long plateLayoutTypeId, long tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(isolateId);
			params.add(box);
			params.add(position);
			params.add("" + plateLayoutTypeId);
			params.add("" + tranid);
			String sql = "spMet_insertBiochemLocation";
			callStoredProc(db, sql, params, true);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred while inserting plate position: "
							+ ex.getMessage());
		}
	}

	/**
	 * updates the biochemLocation table with the isolate id
	 * 
	 * @param box
	 * @param position
	 * @param isolateId
	 * @param tranid
	 * @param db
	 */
	public void updateBiochemLocation(String box, String position,
			String isolateId, String positionId, long tranid, Db db)
	{
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(isolateId);
			params.add(box);
			params.add(position);
			params.add(positionId);
			params.add("" + tranid);
			String sql = "spMet_updateBiochemLocation";
			callStoredProc(db, sql, params, true);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Error occurred while updating plate positions: "
							+ ex.getMessage());
		}
	}

	/**
	 * retrieves the lineage id. If bInsert = true we insert
	 * 
	 * @param domain
	 * @param phylum
	 * @param cls
	 * @param order
	 * @param genus
	 * @param bInsert
	 * @param task
	 * @param db
	 * @return
	 */
	public long insertLineage(String domain, String phylum, String cls,
			String order, String family, String genus, Task task, Db db)
	{
		long id = -1;
		String sId = null;
		try
		{
			String sql = "spMet_insertLineage";
			ArrayList<String> params = new ArrayList<String>();
			params.add(null); // tax
			params.add(null); // lineage
			params.add(domain);
			params.add(phylum);
			params.add(cls);
			params.add(order);
			params.add(family);
			params.add(genus);
			params.add(null); // species
			params.add("" + task.getTranId());

			sId = callStoredProc(db, sql, params, true, false);
			if (sId != null)
				id = Long.valueOf(sId);

		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Unable to insert the lineage id into the database.  Please notify LIMS support.");
		}
		return id;
	}

	public long getLineageId(String domain, String phylum, String cls,
			String order, String family, String genus, Db db)
	{
		long id = -1;
		try
		{
			String sql = "exec spMet_getBiochemLineageId '" + domain + "','"
					+ phylum + "','" + cls + "','" + order + "','" + family
					+ "','" + genus + "'";
			String sId = getDbValue(sql, db);
			if (sId != null)
				id = Long.valueOf(sId);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Unable to retrieve the lineage id from the database.  Please notify LIMS support.");
		}
		return id;
	}

	public long getLineageId(String family, String genus, Db db)
	{
		long id = -1;
		try
		{
			String sql = "exec spMet_getBiochemLineageId '" + family + "','"
					+ genus + "'";
			String sId = getDbValue(sql, db);
			if (sId != null)
				id = Long.valueOf(sId);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(
					"Unable to retrieve the lineage id from the database.  Please notify LIMS support.");
		}
		return id;
	}

	/**
	 * retrieves the biochemIsolateId from the db. If it doesn't exist we insert
	 * 
	 * @param biochemIsolate
	 * @param sample
	 * @param isolationMethod
	 * @param oligos
	 * @param lineageId
	 * @param task
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public long getBiochemIsolateId(String biochemIsolate, String sample,
			String isolationMethod, String oligos, String lineageId,
			String projectId, String notebookPage, String comment, Task task,
			Db db) throws Exception
	{
		long id = -1;
		try
		{
			// lets get the isolate info
			long biochemIsolateItemId = Long.valueOf(getItemId(biochemIsolate,
					"BiochemIsolate", db));
			long sampleItemId = -1;
			long isolationMethodId = -1;
			if (!WtUtils.isNullOrBlank(sample))
				sampleItemId = Long.valueOf(getItemId(sample, ItemType.SAMPLE,
						db));
			if (!WtUtils.isNullOrBlank(isolationMethod))
				isolationMethodId = getAppValueIdAsLong("Isolation Method",
						isolationMethod, null, false, true, task, db);

			// does the isolate exist?
			String isolateId = getDbValue("exec spMet_getBiochemIsolateId "
					+ biochemIsolateItemId, db);

			String biochemIsolateId = null;
			if (WtUtils.isNullOrBlank(isolateId))
			{
				// lets insert
				try
				{
					String sql = "spMet_insertBiochemIsolate";
					ArrayList<String> params = new ArrayList<String>();
					params.add("" + biochemIsolateItemId);
					params.add("" + sampleItemId);
					params.add(oligos);
					params.add("" + isolationMethodId);
					params.add(lineageId);
					params.add(projectId);
					params.add(notebookPage);
					params.add(comment);
					params.add("" + task.getTranId());
					biochemIsolateId = callStoredProc(db, sql, params, true);
					if (!WtUtils.isNullOrBlank(biochemIsolateId))
						id = Long.valueOf(biochemIsolateId);
				}
				catch (Exception ex)
				{
					throw new LinxDbException(
							"Error occurred while inserting new isolate: "
									+ ex.getMessage());
				}

			}
			else
			{
				// lets update
				try
				{
					String sql = "spMet_updateBiochemIsolate";
					ArrayList<String> params = new ArrayList<String>();
					params.add("" + biochemIsolateItemId);
					params.add("" + sampleItemId);
					params.add(oligos);
					params.add("" + isolationMethodId);
					params.add(lineageId);
					params.add(projectId);
					params.add("" + isolateId);
					params.add(notebookPage);
					params.add(comment);
					params.add("" + task.getTranId());
					callStoredProc(db, sql, params, false);
					id = Long.valueOf(isolateId);
				}
				catch (Exception ex)
				{
					throw new LinxDbException(
							"Error occurred while updating isolate: "
									+ ex.getMessage());
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return id;
	}

	public void insertBiochemLocation(String nextPlate, String position,
			long plateLayoutTypeId, Task task, Db db)
	{
		// now lets insert a record into the biochemLocation table
		// does the box and position exist?
		String positionId = getDbValue("exec spMet_getBiochemPositionId '"
				+ nextPlate + "','" + position + "'", db);
		if (WtUtils.isNullOrBlank(positionId))
		{
			insertBiochemLocation(nextPlate, position, null, plateLayoutTypeId,
					task.getTranId(), db);
		}
		else
		{
			throw new LinxDbException("Cannot create biochemLocation for box "
					+ nextPlate + " position " + position
					+ " because it already exists.");
		}
	}

	/**
	 * stores a placeholder record in the biochemIsolate table
	 * 
	 * @param itemid
	 * @param task
	 * @param db
	 * @throws Exception
	 */
	public String insertBiochemIsolateItemId(String itemid, Task task, Db db)
			throws Exception
	{
		// now lets insert a record into the biochemisolation table
		ArrayList<String> params = new ArrayList<String>();
		String procName = "spMet_insertBiochemIsolateItemId";
		params.add(itemid);
		params.add("" + task.getTranId());
		String id = this.callStoredProc(db, procName, params, true, false);
		return id;
	}

	/**
	 * inserts a record into the strainFeatures table
	 * 
	 * @param strainItemId
	 * @param feature
	 * @param data
	 * @param tranId
	 */
	public void insertStrainFeatures(String strainItemId, String feature,
			String data, long tranId, Db db)
	{
		try
		{
			String sql = "spMet_insertStrainFeature";
			ArrayList<String> params = new ArrayList<String>();
			params.add(strainItemId);
			params.add(feature);
			params.add(data);
			params.add(tranId + "");

			callStoredProc(db, sql, params, false, true);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	/**
	 * returns the type for a given item if more than one type exists we throw
	 * an error
	 * 
	 * @param item
	 * @param db
	 * @return
	 */
	public ResultSet getItemType(String item, Db db)
	{
		ResultSet type = null;
		try
		{
			type = this.getResultSet(
					"exec spMet_getTypeForItem '" + item + "'", db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return type;
	}

	/**
	 * retireves the sample type
	 * 
	 * @param item
	 * @param db
	 * @return
	 */
	public String getSampleType(String item, Db db)
	{
		String type = null;
		try
		{
			type = this.getDbValue("exec spEMRE_getSampleType '" + item + "'",
					db);
		}
		catch (Exception ex)
		{

		}
		return type;
	}

	/**
	 * inserts a record into parentDirectory and returns the newly inserted ID
	 * 
	 * @param dir
	 * @param tranid
	 * @param db
	 * @return
	 */
	public long insertParentDirectory(String dir, long tranid, Db db)
	{
		long dirId = -1;
		try
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(dir);
			params.add(tranid + "");
			String sDirID = this.callStoredProc(db,
					"spLinx_InsertParentDirectoryOutput", params, true);
			if (!WtUtils.isNullOrBlank(sDirID))
			{
				dirId = Integer.parseInt(sDirID);
			}
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return dirId;
	}

	/**
	 * retrieves the tranid for a file and path. If one exists returns the
	 * tranid in which the file was created CreateAnyNewAppFiles will check the
	 * tranid to see if it needs to complain for files that need to be new or to
	 * see if it needs to do an insert for cases where bulk loading is looping
	 * through a file and calling save
	 * 
	 * @param parent
	 * @param fileName
	 * @param db
	 * @return tranId if file exists, otherwise null
	 */
	public String doesFileExist(String parent, String fileName, Db db)
	{
		String tranid = null;
		try
		{
			String sql = "exec spEMRE_getAppFileTranId '" + parent + "','"
					+ fileName + "'";
			tranid = this.getDbValue(sql, db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return tranid;
	}

	/**
	 * Updates APPLICATIONFILE core table with applies_to_itemId. App file
	 * record is expected to exist under the given appFileId. Looks up target
	 * itemId and targetType.
	 * 
	 * @param targetItem
	 * @param targetType
	 * @param appFileid
	 * @param tranId
	 * @param db
	 * @throws LinxDbException
	 */
	public void updateAppFileAppliesTo(String targetItem, String targetType,
			String appFileId, long tranId, Db db) throws LinxDbException
	{

		Code.debug("WtDbHelper.updateApplicationFileAppliesTo(" + targetItem
				+ "," + targetType + ",appFile: " + appFileId + ")");

		try
		{
			ArrayList params = new ArrayList();
			params.add(appFileId);
			params.add(targetItem); // may not be null
			params.add(targetType);
			params.add(tranId + "");
			callStoredProc(db, "spLinx_UpdateApplicationFileAppliesTo", params,
					false);
		}
		catch (Throwable ex)
		{
			throw new LinxDbException("Unable to update APPLICATIONFILE: "
					+ ex.getMessage());
		}
	}

	/**
	 * inserts a record into the plasmid table
	 * 
	 * @param plasmidItemId
	 * @param numMarkers
	 * @param plasmidType
	 * @param task
	 * @param db
	 * @return
	 */
	public String insertPlasmid(String plasmidItemId, String numMarkers,
			String plasmidType, Task task, Db db)
	{
		String plasmidId = null;
		try
		{
			String spName = "spEMRE_insertPlasmid";
			ArrayList<String> params = new ArrayList<String>();
			params.add(plasmidItemId);
			params.add(numMarkers);
			params.add(plasmidType);
			params.add(task.getTranId() + "");

			plasmidId = callStoredProc(db, spName, params, true, false);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return plasmidId;
	}

	/**
	 * inserts a list of markers into the plasmidMarker table
	 * 
	 * @param plasmidId
	 * @param lsMarkers
	 * @param task
	 * @param db
	 */
	public void insertPlasmidMarker(String plasmidId, List<String> lsMarkers,
			Task task, Db db)
	{
		try
		{
			String sp = "spEMRE_insertPlasmidMarker";
			ArrayList<String> inputs = new ArrayList<String>();
			int idx = 1;
			for (String marker : lsMarkers)
			{
				inputs.add(plasmidId);
				inputs.add(marker);
				inputs.add(idx + "");
				inputs.add(task.getTranId() + "");
				callStoredProc(db, sp, inputs, false, false);
				inputs.clear();
				inputs = new ArrayList<String>();
				idx++;
			}
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	/**
	 * updates a list of markers in the plasmidMarker table
	 * 
	 * @param plasmidId
	 * @param lsMarkers
	 * @param task
	 * @param db
	 */
	public void updatePlasmidMarker(String plasmidId, List<String> lsMarkers,
			Task task, Db db)
	{

		String sp = "spEMRE_updatePlasmidMarker";
		ArrayList<String> inputs = new ArrayList<String>();
		String markerString = "";
		for (String marker : lsMarkers)
		{				
			markerString = markerString + marker + ";";
		}// next marker
		
		inputs.add(plasmidId);
		inputs.add(markerString);
		inputs.add(task.getTranId() + "");
		callStoredProc(db, sp, inputs, false, false);

	}

	/**
	 * updates the plasmidId in the strain table
	 * 
	 * @param strainItemId
	 * @param plasmidId
	 * @param db
	 */
	public void updateStrainWithPlasmid(String strainItemId, String plasmidId,
			String tranId, Db db)
	{
		try
		{
			String sql = "exec spEMRE_updateStrainWithPlasmid " + strainItemId
					+ "," + plasmidId + "," + tranId;
			executeSQL(sql, db);
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	/**
	 * inserts a record into either sampleLocation or strainLocation dependent
	 * upon the stored proc being called
	 * 
	 * @param itemId
	 *            The item.itemid of the sample
	 * @param rowMap
	 *            The location table from the task UI
	 * @param task
	 * @param request
	 * @param locationColumn
	 *            the integer representing the column in the table that holds
	 *            the location
	 * @param rowset
	 * @param sql
	 * @param db
	 */
	public void insertLocations(String itemId, TableDataMap rowMap,
			String tranid, HttpServletRequest request, int locationColumn,
			String rowset, String sql, Db db)
	{
		try
		{
			int numRows = rowMap.getRowcount();
			int index = 0;
			ArrayList<String> params = new ArrayList<String>();
			for (int rowIdx = 1; rowIdx <= numRows; rowIdx++)
			{
				String location = (String) rowMap.getValue(rowIdx,
						locationColumn);
				index++;
				String[] aLocs = location.split(":");
				String freezer = aLocs[0];
				String box = aLocs[1];
				String position = aLocs[2];
				aLocs = null;
				params.clear();
				params.add(itemId);
				params.add(freezer);
				params.add(box);
				params.add(position);
				params.add(index + ""); // location index
				params.add(tranid);

				callStoredProc(db, sql, params, false, true);
			}
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
	}

	/**
	 * determines if an item is on the "Retired" queue
	 * 
	 * @param item
	 * @param db
	 * @return
	 */
	public boolean isRetired(String item, Db db)
	{
		boolean bRetired = false;
		try
		{
			String task = getDbValue(
					"exec spEMRE_isItemRetired '" + item + "'", db);
			if (!WtUtils.isNullOrBlank(task))
				bRetired = true;
		}
		catch (Exception ex)
		{
			throw new LinxUserException(ex.getMessage());
		}
		return bRetired;
	}

	/**
	 * retrieves the primary key from the cultureCollection table
	 * 
	 * @param cultureItemId
	 * @param db
	 * @return
	 */
	public long getCultureCollectionId(String cultureItemId, Db db)
	{
		long id = -1;
		try
		{
			String sId = getDbValue("exec spEMRE_getCultureCollectionId "
					+ cultureItemId, db);
			if (!WtUtils.isNullOrBlank(sId))
				id = Long.parseLong(sId);
			else
				throw new Exception(
						"Unable to retrieve cultureCollectionId from the database.");
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return id;
	}
	

	/**
	 * Determine if an item exists regardless of type
	 * including non-item SamplingID to support EMRE v2.1.
	 * 
	 * @item the input item
	 * @db the Db object
	 * @return a boolean representing the existence of the item
	 */
	public boolean isEMREItemExisting(String item, Db db)
	{
		boolean bExists = false;
		try
		{
			String val = getDbValue("exec spEMRE_doesItemExist '" + item + "'",
					db);
			if (!WtUtils.isNullOrBlank(val))
				bExists = true;
		}
		catch (Exception ex)
		{
			throw new LinxDbException(ex.getMessage());
		}
		return bExists;
	}

	/**
	 * Returns true if the given name matches a samplingID
	 * in the samplingID field of samplingTimepoint custom table.
	 * Sampling IDs are not tracked as core items in this version of LIMS.
	 * @param name
	 * @param db
	 * @return
	 */
	public boolean isSamplingID(String name, Db db)
	{
		String val = this.getDbValueFromStoredProc(db, "spEMRE_isSamplingID", name);
		return Boolean.valueOf(val);
	}

	/**
	 * Does the db update to add the given data file from the shared
	 * data folder as an appfile applied to the given item.
	 * Called by EMRE 2.1 copyFolder/File recursive routine
	 * for all tasks that upload on demand from the shared data folder.
	 * @param parentPath
	 * @param filenameOnly
	 * @param filetype
	 * @param appliesTo
	 * @param itemtype
	 * @param tranId
	 * @param db
	 */
	public void addAppFileForSharedDataFile(String parentPath, String filenameOnly,
			String fileType, String appliesTo, String itemType, long tranId, Db db)
	{
		ArrayList<String> params = new ArrayList<String>();
		params.add(parentPath);
		params.add(filenameOnly);
		params.add(fileType);
		params.add(appliesTo);
		params.add(itemType);
		params.add(tranId+"");
		
		String appFileId =
			getDbValueFromStoredProc(db, "spEMRE_insertAppFileForDataFile", params);
		
	}
	
}
