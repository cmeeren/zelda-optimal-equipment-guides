package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds Entry instances associated with a GameSegment
 */
public class Entries implements Iterable<Entry> {

    private List<Entry> entries = new ArrayList<>();

    public Entries(Element sectionElement) {
        NodeList entryList = sectionElement.getElementsByTagName("entry");
        for (int i=0; i<entryList.getLength(); i++) {
            Element entryElement = (Element) entryList.item(i);
            entries.add(new Entry(entryElement));
        }
    }

    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

}
