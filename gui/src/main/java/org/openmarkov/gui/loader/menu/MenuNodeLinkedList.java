/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;

import java.util.LinkedList;

/**
 * MenuNodeLinkedList defines an object to manipulate MenuNode objects prior to
 * define the final menus in the OpenMarkov Tool
 *
 * @author jlgozalo
 * @version 1.0 18/11/2008
 */
public class MenuNodeLinkedList {

	private LinkedList<MenuNode> menuList;

	/**
	 * constructor
	 */
	MenuNodeLinkedList() {

		this.menuList = new LinkedList<>();
	}

	/**
	 * Look for the Menunode element in the list and returns the position
	 *
	 * @param menuNode Menunode element
	 * @return the position of the first ocurrence of a MenuNode element equal
	 * to the specified element or -1 if no matching element is found
	 */
	public int indexOf(MenuNode menuNode) {

		return this.menuList.indexOf(menuNode);
	}

	/**
	 * Look for the name of the sName element in the list and returns the
	 * position
	 *
	 * @param sName sName element
	 * @return the position of the first ocurrence of a MenuNode element equal
	 * to the specified element or -1 if no matching element is found
	 */
	public int indexOf(String sName) {

		int pos = -1;
		for (MenuNode nodo : menuList) {
			pos++;
			if (nodo.getName().equals(sName)) {
				// found!!!
				return pos;
			}
		}
		return pos;
	}

	/**
	 * @return the menuList
	 */
	public LinkedList<MenuNode> getMenuList() {

		return menuList;
	}

	/**
	 * @param menuList the menuList to set
	 */
	public void setMenuList(LinkedList<MenuNode> menuList) {

		this.menuList = menuList;
	}

	/**
	 * @return the size
	 */
	public int getSize() {

		if (menuList != null) {
			return menuList.size();
		}
		return 0;

	}

	/**
	 * @return true if the element is empty (list=null)
	 */
	public boolean isEmpty() {
        
        return (getSize() == 0);
	}

	/**
	 * get the element at the required position
	 */
	public MenuNode getElementAt(int indexPos) {

		return this.menuList.get(indexPos);
	}

	/**
	 * toString method
	 *
	 * @return String the element
	 */
	public String toString() {
		String out = "[MenuNodeLinkedList -> size= " + getSize() + " ,elements= [";
		if (!this.isEmpty()) {
			for (MenuNode node : this.menuList) {
				out += "\n\t\t " + node.toString();
			}
		}
		out += " ] ]";
		return out;
	}

}
