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
public class Shortlist {

	@DbColumnName("PROPERTY_ID")
	private Integer myPropertyID;
	@DbColumnName("IS_FAVOURITE")
	private int myIsFavourite;
	@DbColumnName("PROPERTY_POSTCODE")
	private Integer myPostcode;
	@DbColumnName("PROPERTY_ADDRESS")
	private String myAddress;


	public int getPropertyID() {
		return myPropertyID;
	}

	public void setPropertyID(int propertyID) {
		myPropertyID = propertyID;
	}

	public int getIsFavourite() {
		return myIsFavourite;
	}

	public void setIsFavourite(int isFavourite) {
		myIsFavourite = isFavourite;
	}

	public int getPostcode() {
		return myPostcode;
	}

	public void setPostcode(int postcode) {
		myPostcode = postcode;
	}

	public String getAddress() {
		return myAddress;
	}

	public void setAddress(String address) {
		myAddress = address;
	}
}
