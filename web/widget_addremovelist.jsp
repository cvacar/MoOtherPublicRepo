<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<script language="JavaScript" type="text/javascript">
<!--

var isFirefox = ( navigator.appVersion.indexOf( "Mozilla" )== -1)? false : true;
var isMSIE = (navigator.appVersion.indexOf("MSIE")== -1)? false : true;

function moveOptions(src, target)
{
	if(isMSIE)
	{
		moveOptionsIE(src, target);
	}
	else
	{
		moveOptionsFF(src, target);
	}
}

function addOption(theSel, theText, theValue)
{
  var newOpt = new Option(theText, theValue);
  var selLength = theSel.length;
  theSel.options[selLength] = newOpt;
}

function deleteOption(theSel, theIndex)
{ 
  var selLength = theSel.length;
  if(selLength>0)
  {
    theSel.options[theIndex] = null;
  }
}

function moveOptionsFF(theSelFrom, theSelTo)
{
  
  var selLength = theSelFrom.length;
  var selectedText = new Array();
  var selectedValues = new Array();
  var selectedCount = 0;
  
  var i;
  
  // Find the selected Options in reverse order
  // and delete them from the 'from' Select.
  for(i=selLength-1; i>=0; i--)
  {
    if(theSelFrom.options[i].selected)
    {
      selectedText[selectedCount] = theSelFrom.options[i].text;
      selectedValues[selectedCount] = theSelFrom.options[i].value;
      deleteOption(theSelFrom, i);
      selectedCount++;
    }
  }
  
  // Add the selected text/values in reverse order.
  // This will add the Options to the 'to' Select
  // in the same order as they were in the 'from' Select.
  for(i=selectedCount-1; i>=0; i--)
  {
    addOption(theSelTo, selectedText[i], selectedValues[i]);
  }
}



function moveRight(){
    var src = document.getElementById('existing');
    var target = document.getElementById('added');
    moveOptions(src, target);
    }

function moveLeft(){
    var src = document.getElementById('added');
    var target = document.getElementById('existing');
    moveOptions(src, target);
    }
   
function moveOptionsIE(src, target){
    var selectedOption;

    if ( src.selectedIndex<0 )
    {
         alert('Please select at least one option');
         return;
         }
   
      //for multiple selection:
      var selectedValues = new Array();
    	for (ind=0; ind<src.options.length; ind++){
            if( src.options[ ind ].selected)
                  selectedValues[ selectedValues.length] = ind;
            }      
             
       for (ind2=0; ind2<selectedValues.length; ind2++)
          target.options[ target.options.length ] = new Option( src.options[selectedValues[ind2]].text , src.options[selectedValues[ind2]].value );
            
       src.options.remove( src.selectedIndex );
     }     

function placeInHidden(delim)
{
	var added = document.getElementById('added');
	var hideObj = document.getElementById('addedVals');
  	hideObj.text = '';
    for (ind2=0; ind2<added.length; ind2++)
    {
    	if(ind2 == added.length - 1)
    		hideObj.text +=  added.options[ind2].text
    	else
    		hideObj.text +=  added.options[ind2].text + delim;
    }
     //alert('Hidden field:' + hideObj.text);
     hideObj.value = hideObj.text;
    
}
	

</script>


<table border="0">
	<tr>
		<td><b>Available</b></td>
		<td></td>
		<td><b>Added</b></td>
	</tr>
	<tr>
		<td>
			<select style="width: 8em;" name="existing" id="existing" size="10" multiple="multiple">
			<c:forEach var="opt" items="${item.displayValues}">
  				<c:choose>
  					<c:when test="${item.selectedValue==opt}">
    					<option name="${opt}" selected="true">${opt}</option>
 					</c:when>
 					<c:otherwise>
     					<option name="${opt}">${opt}</option>
 					</c:otherwise>
 				</c:choose>
  			</c:forEach>
		</td>
		<td valign="middle">
			<input type="hidden" name="addedVals" id="addedVals" value="">
			<input type="button" name="add" id = "add" value="&gt;&gt;" 
				onclick="moveRight('existing'); placeInHidden(';');"/><br/>
			<input type="button" name="remove" id="remove" value="&lt;&lt;"
				onclick="moveLeft('added'); placeInHidden(';');"/>
		</td>
		<td>
			<select style="width: 8em;" id="added" name="added" size="10" multiple="multiple">
				<c:forEach var="opt" items="${addedOptions}">
     				<c:choose>
  					<c:when test="${addedOptions.selectedValue==opt}">
    					<option name="${opt}" selected="true" >${opt}</option>
 					</c:when>
 					<c:otherwise>
     					<option name="${opt}">${opt}</option>
 					</c:otherwise>
 				</c:choose>
  				</c:forEach>
			</select>
		</td>
	</tr>
</table>