package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds Item instances
 */
public class Items implements Iterable<Item> {

    private Map<String, Item> items = new LinkedHashMap<>();

    public Items(Document doc) {
        NodeList itemList = doc.getElementsByTagName("item");
        for (int i=0; i<itemList.getLength(); i++) {
            Element itemNode = (Element) itemList.item(i);
            String id = itemNode.getAttribute("id");
            items.put(id, new Item(itemNode));
        }
    }

    /**
     * Returns the Item associated with an item ID.
     * @param id item ID
     * @return the Item with this ID
     */
    public Item get(String id) {
        return items.get(id);
    }

    /**
     *
     * @param id item ID
     * @return true if this ID is present in the item list
     */
    public boolean hasID(String id) {
        return items.containsKey(id);
    }

    public Iterator<Item> iterator() {
        return items.values().iterator();
    }

}
