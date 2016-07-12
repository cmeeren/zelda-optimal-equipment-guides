package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class to hold an &lt;item&gt; element.
 *
 * In addition to holding all information initially defined in the guide,
 * it also keeps track of the current count of this item throughout the guide.
 */
public class Item {

    public String id;
    public boolean unique = true;
    public Name name;
    public int count = 0;
    public String summaryType;
    public boolean displayInSummaryIfZero = false;
    public int numberOfParts = 1;
    public int currentParts = 0;
    public String partsOf;
    public String gameVariations;
    public List<String> filterIDs = new ArrayList<>();
    public LinkedHashMap<String, Name> otherNames = new LinkedHashMap<>();

    public Item(Element itemElement) {
        // id
        id = itemElement.getAttribute("id");

        // unique
        if (itemElement.hasAttribute("unique")) {
            unique = Boolean.parseBoolean(itemElement.getAttribute("unique"));
        }

        // number of starting items
        if (itemElement.hasAttribute("startNum")) {
            count = Integer.parseInt(itemElement.getAttribute("startNum"));
        }

        // display in which summary list?
        summaryType = itemElement.getAttribute("summaryType");

        // should the item be displayed in summary if we have no items?
        if (itemElement.hasAttribute("displayIfZero")) {
            displayInSummaryIfZero = Boolean.parseBoolean(itemElement.getAttribute("displayIfZero"));
        }

        // number of parts
        if (itemElement.hasAttribute("numParts")) {
            numberOfParts = Integer.parseInt(itemElement.getAttribute("numParts"));
        }

        // id of the item which is the parts of this item
        if (itemElement.hasAttribute("partsOf")) {
            partsOf = itemElement.getAttribute("partsOf");
        }

        // number of parts to start with
        if (itemElement.hasAttribute("startParts")) {
            currentParts = Integer.parseInt(itemElement.getAttribute("startParts"));
        }

        if (itemElement.hasAttribute("gameVariations")) {
            gameVariations = itemElement.getAttribute("gameVariations");
        }

        // filters
        if (itemElement.hasAttribute("filters")) {
            filterIDs = Arrays.asList(itemElement.getAttribute("filters").split("\\s*,\\s*"));
        }

        // name
        NodeList names = itemElement.getElementsByTagName("name");
        for (int i=0; i<names.getLength(); i++) {
            Name name = new Name((Element) names.item(i));
            if (name.id != null) {
                otherNames.put(name.id, name);
            } else {
                this.name = name;
            }
        }
    }

    /**
     * Updates the name of the item.
     *
     * @param id ID of the new name
     */
    public void updateName(String id) {
        name = otherNames.get(id);
    }

    /**
     * Returns true if this item is associated with a summary list, false otherwise
     */
    public boolean isInSummaryList() {
        return summaryType != null && !summaryType.trim().equals("");
    }

    /**
     * Returns true if this item is part of another item (such as Heart Pieces), false otherwise
     */
    public boolean isPart() {
        return partsOf != null && !partsOf.trim().equals("");
    }

    /**
     * Returns true if this item is associated with specific game variations, false otherwise
     */
    public boolean hasGameVariations() {
        return gameVariations != null && !gameVariations.trim().equals("");
    }

    /**
     * Add parts to this item
     */
    public void addParts(int increment) {
        currentParts += increment;

        // we may have a new whole
        count += currentParts / numberOfParts;
        currentParts = currentParts % numberOfParts;
    }

    /**
     * Inner class to hold an item name (singular/plural variations and name ID)
     */
    public class Name {
        public String singular;
        public String plural;
        public String id;

        public Name(Element name) {

            if (name.hasAttribute("id")) id = name.getAttribute("id");

            NodeList singularElements = name.getElementsByTagName("singular");
            NodeList pluralElements = name.getElementsByTagName("plural");

            if (singularElements.getLength() == 0 && pluralElements.getLength() == 0) {
                // no singular/plural specified im XML, assume text contents
                // of <name> tag is singular and append "s" for plural
                singular = name.getTextContent();
                plural = singular + "s";
            } else if (singularElements.getLength() == 1 && pluralElements.getLength() == 1) {
                // singular/plural specified im XML
                singular = singularElements.item(0).getTextContent();
                plural = pluralElements.item(0).getTextContent();
            } else {
                System.out.println("<name> element has unsupported number of <singular> and <plural> child elements");
            }
        }
    }

}
