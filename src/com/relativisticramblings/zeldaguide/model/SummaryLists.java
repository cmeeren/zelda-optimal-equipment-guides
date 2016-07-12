package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a map of SummaryList objects keyed by their IDs
 */
public class SummaryLists implements Iterable<SummaryList> {

    private Map<String, SummaryList> summaryLists = new LinkedHashMap<>();

    public SummaryLists(Document doc) {

        NodeList summaryListElements = doc.getElementsByTagName("summaryList");

        // initialize SummaryList object for each <summaryList> element and add to map
        for (int i=0; i<summaryListElements.getLength(); i++) {
            Element summaryListElement = (Element) summaryListElements.item(i);
            String id = summaryListElement.getAttribute("id");
            SummaryList summaryList = new SummaryList(summaryListElement);
            summaryLists.put(id, summaryList);
        }

    }

    public SummaryList getByID(String id) {
        return summaryLists.get(id);
    }

    public Iterator iterator() {  // FIXME: Should the type be Iterator<SummaryList>?
        return summaryLists.values().iterator();
    }

    public Collection<SummaryList> values() {
        return summaryLists.values();
    }

}
