package com.sgsi.emre.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sgsi.emre.EMREStrings.ItemType;
import com.sgsi.emre.bean.DataType;
import com.wildtype.linx.db.Db;
import com.wildtype.linx.log.Code;
import com.wildtype.linx.task.Item;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;
import com.wildtype.linx.util.WtUtils;

public class StrainTaxonomicId extends EMRETask 
{
	String copyDirectory = null;
	long copyDirectoryId = -1;
	@Override
	public void doTaskWorkPreSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		
		try
		{
			copyDirectory = null;
			copyDirectoryId = -1;
			//lets get the file item if it exists
			Item fileItem = getServerItem("SequenceResultFile");
			String seqFile = fileItem.getValue();
			
			if(!WtUtils.isNullOrBlankOrPlaceholder(seqFile))
			{
				String fqPath = getFullyQualifiedPath(request, seqFile);
				File srcFile = new File(fqPath);
				//the list of files to be copied
				String parentDir = srcFile.getParent();
				if(WtUtils.isNullOrBlank(parentDir))
					throw new LinxSystemException("Cannot determine the parentDirectory for: " + fqPath);
				File pd = new File(parentDir);
				File[] dirList = null;
				if(pd.isDirectory())
				{
					if(!pd.canRead())
						throw new LinxSystemException("Cannot read directory: " + pd.getPath() + ".\r\nPlease make sure the directory is shared.");
					dirList = pd.listFiles();
				}
				if(dirList == null)
					throw new LinxSystemException("Cannot retrieve files from directory : " + pd.getPath());
				//we need to copy all of the files to a directory on the file server
				String sequenceFileDirectory = dbHelper.getApplicationValue(db, "System Properties", 
						"Sequencing File Path");
				if(WtUtils.isNullOrBlank(sequenceFileDirectory))
					throw new LinxSystemException("Unable to determine the storage location for sequencing files.  Please notify LIMS Support.");
				String item = getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
				if(WtUtils.isNullOrBlank(item))
					throw new LinxUserException("Please provide a value for required item 'New LIMS ID'");
				item = item.trim().toUpperCase();
				String copyLocation = sequenceFileDirectory + item;
				String batContents = "mkdir " + copyLocation + "\r\n";
				//ok now we've got the folder for the LIMS Id 
				//do we have a subfolder - if we don't lets make one
				File location = new File (copyLocation);
				int currentRead = 1;
				if(!location.exists())
				{
					//this is the first time we've stored data for this ID.
					//make a subfolder called "1" and we will use it for writing files
					currentRead = 1;
				}
				else
				{
					//we've already written to this directory
					//lets see what number we're on
					if(location.isDirectory())
					{
						File[] files = location.listFiles();
						for(File f : files)
						{
							if(f.isDirectory())
							{
								String name = f.getPath();
								String dirNum = name.substring(name.lastIndexOf(File.separator) + 1);
								try
								{
									//lets see if this is a sequencing dir
									currentRead = Integer.parseInt(dirNum);
									currentRead++;
								}
								catch(Exception ex)
								{
									//not a numeric directory - lets ignore
									continue;
								}
							}
							f = null;
						}
						files = null;
					}
				}
				copyLocation = sequenceFileDirectory + item  + File.separator + EMRETask.zeroPad(currentRead, 3) + File.separator;
				try
				{
					//synchronize so only one user enters here at a time 
					synchronized (this)
	                {
						batContents += "mkdir \"" + copyLocation + "\"\r\n xcopy \"" + parentDir + "\"\\*.* \"" + copyLocation + "\" /e \r\nexit";
						Code.debug(batContents);
						File f = new File("batchFile.bat");
						FileOutputStream fos = new FileOutputStream(f);
						fos.write(batContents.getBytes());
						fos.close();
						fos = null;
						try
						{
							String command = "cmd /C start batchFile.bat ";
							Runtime rt = Runtime.getRuntime();
							Process pr = rt.exec(command);
							Thread.sleep(3500);
							int iCompleted = pr.exitValue();
							if(iCompleted != 0)
								throw new Exception("Unable to create the directory and/or copy files to: " + copyLocation + ".\r\n  Please notify IT Support.");
						}catch (IOException e) 
						{
							e.printStackTrace();
							throw new Exception(e.getMessage());
						}

					
						if(f.exists())
							f.delete();
						f = null;
						
	                }
				}
				catch(Exception ex)
				{
					throw new Exception(ex.getMessage());
				}
				copyDirectory = copyLocation;
				
				//did we make our copy directory?
				File copyDir = new File(copyLocation);
				if(!copyDir.exists())
					throw new LinxUserException("Unable to create the directory to store sequence files: " + copyLocation + ".\r\n  Please notify IT Support.");
				Code.debug("Finished creating directories.");
				//does the copy directory actually contain files?
				File[] copiedFiles = copyDir.listFiles();
				if(copiedFiles == null || copiedFiles.length < 1)
					throw new LinxSystemException("Unable to copy files to directory: " + copyLocation + ".  Please notify IT support.");
				copyDir = null;
				//are the dirList and the copiedFiles the same or did we miss some?
				//lets just check for the number of files to be the same.
				int origNum = dirList.length;
				int copyNum = copiedFiles.length;
				if(origNum != copyNum)
					throw new LinxSystemException("Unable to copy all files from " + parentDir + " to : " + copyLocation);
				//lets loop through the directory and copy the files
//				Code.debug("Starting to copy files to destination directory.");
//				for(File f : dirList)
//				{
//					String fileName = f.getParent() + File.separator + f.getName();
//					Code.debug("Filename of file to copy is: " + fileName);
//					try
//					{
//						fqPath = getFullyQualifiedPath(request, fileName);
//						if(WtUtils.isNullOrBlank(fqPath))
//							throw new LinxSystemException("Unable to determine fully qualified path to file.");
//					}
//					catch(Exception ex)
//					{
//						throw new Exception(ex.getMessage());
//					}
//					Code.debug("Fully qualified path of file to copy is: " + fqPath);
//					Item i = getServerItem("SequenceResultFile");
//					i.setValue(fqPath);
//					try
//					{
//						this.copyFileToNet(i, copyDirectory, "SequenceResultFile", null, db);
//						Code.debug("Finished copying file: " + fqPath + "\r\nto path: " + copyDirectory);
//					}
//					catch(Exception ex)
//					{
//						throw new Exception (ex.getMessage());
//					}
//					String newFileName = copyLocation + f.getName();
//					File nf = new File(newFileName);
////					this.copyFile(fqPath, newFileName); 
//					alCopiedFiles.add(nf);
//					nf = null;
//				}
//				Code.debug("Finished copying files to destination directory.");
			}
			//if we made it this far we've copied all of the files
			
			//let the user know 
			if(!WtUtils.isNullOrBlank(copyDirectory))
			{
				//insert a record into parentDirectory for the copied files
				copyDirectoryId = dbHelper.insertParentDirectory(copyDirectory, getTranId(), db);
				setMessage("Successfully saved taxonomic information and copied sequencing files to path:\r\n" + copyDirectory);
			}
			else
				setMessage("Successfully saved taxonomic information.");
		}
		catch(Exception ex)
		{
			cleanUpFiles();
			throw new LinxSystemException(ex.getMessage());
		}
		
	}
	
	@Override
	public void doTaskWorkPostSave(HttpServletRequest request, HttpServletResponse response, User user, Db db)
	{
		  updateCustomTables(request, db);
	}

	/**
	 * Inserts new or updates into custom table.
	 * @param db
	 */
	protected void updateCustomTables(HttpServletRequest request, Db db)
	{
		String item = getServerItemValue(ItemType.SECONDARY_ENRICHMENT);
		String itemId = dbHelper.getItemId(item, ItemType.SECONDARY_ENRICHMENT, db);
		String notebook = getServerItemValue(DataType.NOTEBOOK_REF);
		String sampleType = getServerItemValue("SampleType");
		String seqVendor = getServerItemValue("SequencingVendor");
		String dateSubmitted = getServerItemValue("DateSubmitted");
		String vendorId = getServerItemValue("VendorID");
		//String seqResultsFile =getServerItemValue("SequenceResultFile");
		
		ArrayList<String> params = new ArrayList<String>();
	 	String sql = "spMet_insertOrUpdateSeqParameters";
	 	params.add(itemId);
	 	params.add(notebook);
	 	params.add(sampleType);
	 	params.add(seqVendor);
	 	params.add(dateSubmitted);
	 	params.add(vendorId);
	 	if(copyDirectoryId == -1)
	 		params.add(null);
	 	else
	 		params.add(copyDirectoryId + "");
	 	params.add(getTranId() + "");
	 	dbHelper.callStoredProc(db, sql, params, false, true);
	 	
	}
	
	public void cleanUpFiles()
	{
		try
		{
			//delete the newly created copy directory.
			if(!WtUtils.isNullOrBlank(copyDirectory))
			{
				File nf = new File(copyDirectory);
				if(nf.exists())
					nf.delete();
			}
			
		}
		catch(Exception ex)
		{
			//ignore
		}
	}
}
