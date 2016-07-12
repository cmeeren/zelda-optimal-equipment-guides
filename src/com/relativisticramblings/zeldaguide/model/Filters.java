package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a list of filters
 */
public class Filters implements Iterable<Filter> {

    private Map<String, Filter> filters = new LinkedHashMap<>();

    public Filters(Document doc) {

        NodeList filterElements = doc.getElementsByTagName("filter");

        // initialize Filter object for each <filter> element and add to map
        for (int i=0; i<filterElements.getLength(); i++) {
            Element filterElement = (Element) filterElements.item(i);
            String id = filterElement.getAttribute("id");
            Filter filter = new Filter(filterElement);
            filters.put(id, filter);
        }

    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public Iterator<Filter> iterator() {
        return filters.values().iterator();
    }

}
