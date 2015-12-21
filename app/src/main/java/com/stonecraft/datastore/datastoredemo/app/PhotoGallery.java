/**
 * 
 */
package com.stonecraft.datastore.datastoredemo.app;

import android.graphics.Bitmap;

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
public class PhotoGallery {

	@DbColumnName("IMAGE_ID")
	private Integer myId;
	@DbColumnName("PHOTO_DATA")
	private Bitmap myImage;

	public Integer getId() {
		return myId;
	}

	public void setId(Integer id) {
		myId = id;
	}

	public Bitmap getImage() {
		return myImage;
	}

	public void setImage(Bitmap image) {
		myImage = image;
	}
}
