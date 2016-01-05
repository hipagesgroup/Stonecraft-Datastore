/**
 * 
 */
package com.stonecraft.datastore.datastoredemo.app;

import com.stonecraft.datastore.DbColumnName;

/**
 * This class
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created May 2, 2014
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class ShortlistJoin {

	@DbColumnName("PROPERTY_ID")
	private Integer myShortListPropertyID;
	@DbColumnName("ID")
	private Integer myShortListId;
	@DbColumnName("PROPERTY_POSTCODE")
	private Integer myShortListPostcode;

	public Integer getShortListPropertyID() {
		return myShortListPropertyID;
	}

	public void setShortListPropertyID(Integer shortListPropertyID) {
		myShortListPropertyID = shortListPropertyID;
	}

	public Integer getShortListId() {
		return myShortListId;
	}

	public void setShortListId(Integer shortListId) {
		myShortListId = shortListId;
	}

	public Integer getShortListPostcode() {
		return myShortListPostcode;
	}

	public void setShortListPostcode(Integer shortListPostcode) {
		myShortListPostcode = shortListPostcode;
	}
}
