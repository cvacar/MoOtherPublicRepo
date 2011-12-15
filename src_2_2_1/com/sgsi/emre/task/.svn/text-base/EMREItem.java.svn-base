package com.sgsi.emre.task;

import org.w3c.dom.Element;

import com.wildtype.linx.task.Task;
import com.wildtype.linx.util.WtUtils;

public class EMREItem extends com.wildtype.linx.task.Item
{
	
    public EMREItem(Element e)
	{
		super(e);
		// TODO Auto-generated constructor stub
	}

	public EMREItem(Task task, String itemType)
	{
		super(task, itemType);
		// TODO Auto-generated constructor stub
	}

	/**
     * Gets value of 'new' element under this.
     * @return Returns true if the item is flagged as optional, doesn't have to be new
     */
    public boolean isNewOptional()
    {
      String sNew = this.eItem.getAttribute(NEW);
      if(WtUtils.isNullOrBlank(sNew))
      {
    	  return false;
      }
      else if( sNew.equalsIgnoreCase("optional"))
      {
        return true;
      }
      else if( sNew.equalsIgnoreCase("oncePerTran"))
      {
    	 return true; 
      }
      return false;
    }

}
