package com.sgsi.emre.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.wildtype.linx.db.Db;
import com.wildtype.linx.task.Task;
import com.wildtype.linx.user.User;
import com.wildtype.linx.util.LinxSystemException;
import com.wildtype.linx.util.LinxUserException;

public class UpdateAnimalSample extends UpdateEnvironmentalSample 
{

	public void setDisplayItemValues(String sample, Task task, 
			  User user, HttpServletRequest request, Db db)
		{
			// set values for the search item
			ArrayList<String> params = new ArrayList<String>();
			params.add(sample);

			try
			{
				// retrieves newest comments first
				ResultSet rs = db.getHelper().getResultSetFromStoredProc(db,
						"spEMRE_getAnimalSampleData", params, true);
				int numRows = 0;
				while (rs.next())
				{
					numRows++;
					task.getDisplayItem("InternalID").setValue(rs.getString(2));
					task.getDisplayItem("FieldName").setValue(
							rs.getString(3));
					task.getDisplayItem("Description").setValue(
							rs.getString(4));
					task.getDisplayItem("Taxonomy").setValue(rs.getString(5));
					task.getDisplayItem("Tissue").setValue(rs.getString(6));
					task.getDisplayItem("Volume_liters").setValue(rs.getString(7));
					task.getDisplayItem("Weight_grams").setValue(rs.getString(8));
					task.getDisplayItem("Temperature_C").setValue(rs.getString(9));
					task.getDisplayItem("pH").setValue(rs.getString(10));
					task.getDisplayItem("Latitude").setValue(rs.getString(11));
					task.getDisplayItem("Longitude").setValue(rs.getString(12));
					task.getDisplayItem("Altitude_m").setValue(rs.getString(13));
					task.getDisplayItem("SiteDescription").setValue(rs.getString(14));
					task.getDisplayItem("ClosestTown").setValue(rs.getString(15));
					task.getDisplayItem("City").setValue(rs.getString(16));
					task.getDisplayItem("County").setValue(rs.getString(17));
					task.getDisplayItem("State").setValue(rs.getString(18));
					task.getDisplayItem("Country").setValue(rs.getString(19));
					task.getDisplayItem("StorageMethod").setValue(rs.getString(20));
					task.getDisplayItem("ArchiveLocation").setValue(rs.getString(21));
					task.getDisplayItem("Comment").setValue(rs.getString(22));
					break;

				}// expecting only one row back, 
				rs.close();
				rs = null;
				// at exit, have set UI properties for this sample, if known
				if(numRows == 0)
					throw new LinxUserException("There were no rows returned from the database.\r\n" + 
							"Please ensure that you have an animal sample and try again.");
			}
			catch (SQLException e)
			{
				throw new LinxSystemException("While retrieving animal sample properties: "
						+ e.getMessage());
			}
		}
}
