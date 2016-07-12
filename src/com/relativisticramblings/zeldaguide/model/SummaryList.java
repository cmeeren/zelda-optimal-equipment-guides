package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a summary list and holds a list of item IDs
 * for that summary list as well as a list of which items have been updated/added
 * in the current segment of the guide
 */
public class SummaryList {

    private String id;
    private String title;
    private List<String> itemIDs;
    private List<String> updatedItemIDs;

    public SummaryList(Element summarySectionElement) {
        id = summarySectionElement.getAttribute("id");
        title = summarySectionElement.getTextContent();
        itemIDs = new ArrayList<>();
        updatedItemIDs = new ArrayList<>();
    }

    public void addItem(String itemID) {
        if (!itemIDs.contains(itemID)) itemIDs.add(itemID);
        if (!updatedItemIDs.contains(itemID)) updatedItemIDs.add(itemID);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getItemIDs() {
        return itemIDs;
    }

    public boolean isUpdated(String itemID) {
        return updatedItemIDs.contains(itemID);
    }

    public void markAllItemsOld() {
        updatedItemIDs.clear();
    }

}
