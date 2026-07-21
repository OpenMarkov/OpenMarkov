/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.element;

import java.awt.*;

/**
 * OpenMarkovLogoIcon encapsulates in a single class the icon to be used in
 * frames and be easy to maintain
 *
 * @author jlgozalo
 * @version 1.0 14 Jun 2009
 */
public class OpenMarkovLogoIcon {

	/**
	 * IconBind for the Main OpenMarkov Frame
	 */
	static final String OPENMARKOV_LOGO_IMAGEICON_16 = "/icons/openmarkov.png";

	/**
	 * OpenMarkovLogoIcon unique instance. Used in singleton pattern.
	 */
	private static final OpenMarkovLogoIcon INSTANCE = new OpenMarkovLogoIcon();

	/**
	 * default constructor
	 */
	private OpenMarkovLogoIcon() {

	}

	/**
	 * the unique instance for this object
	 *
	 * @return OpenMarkovLogoIcon single instance (singleton pattern)
	 */
	public static OpenMarkovLogoIcon getUniqueInstance() {
		return INSTANCE;
	}

	/**
	 * retrieves the openmarkov logo image for 16 points
	 *
	 * @return the image for 16 points
	 */
	public Image getOpenMarkovLogoIconImage16() {
		return Toolkit.getDefaultToolkit().getImage(getClass().getResource(OPENMARKOV_LOGO_IMAGEICON_16));
	}

}
