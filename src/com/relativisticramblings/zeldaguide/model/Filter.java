package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;

/**
 * This class holds a <filter> element. Filters are used for hiding/showing entries in the guide.
 * Each filter has an id and a title.
 */
public class Filter {

    public String id;
    public String title;

    public Filter(Element filterElement) {
        id = filterElement.getAttribute("id");
        title = filterElement.getTextContent();
    }
}
