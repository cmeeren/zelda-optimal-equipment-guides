package com.relativisticramblings.zeldaguide.model;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Represents a single entry (item/description) in the guide
 */
public class Entry {

    public String id;
    public String itemRef;
    public boolean checkbox = true;
    public int add = 1;
    public String changeNameID;
    public Map<String, String> descriptions = new LinkedHashMap<>();
    public String appendToName;
    public String prefix;
    public String nameOverride;
    public List<String> filterIDs = new ArrayList<>();
    public String heading;

    public Entry(Element entryElement) {

        // This is a really ugly hack for supporting subheadings in sections via <entry type="subheading">title</entry>
        if (entryElement.hasAttribute("type") && entryElement.getAttribute("type").equals("subheading")) {
            heading = entryElement.getTextContent();
            return;
        }

        id = entryElement.getAttribute("id");

        if (entryElement.hasAttribute("itemRef"))
            itemRef = entryElement.getAttribute("itemRef");

        if (entryElement.hasAttribute("checkbox"))
            checkbox = Boolean.parseBoolean(entryElement.getAttribute("checkbox"));

        if (entryElement.hasAttribute("add"))
            add = Integer.parseInt(entryElement.getAttribute("add"));

        if (entryElement.hasAttribute("changeNameID"))
            changeNameID = entryElement.getAttribute("changeNameID");

        NodeList descriptionElements = entryElement.getElementsByTagName("description");
        for (int i = 0; i < descriptionElements.getLength(); i++) {
            Element descriptionElement = (Element) descriptionElements.item(i);
            String description = descriptionElement.getTextContent();
            if (descriptionElement.hasAttribute("gameVariations")) {
                String gameVariations = descriptionElement.getAttribute("gameVariations");
                descriptions.put(gameVariations, description);
            } else {
                descriptions.put("all", description);
            }
        }

        NodeList appendToNameNodes = entryElement.getElementsByTagName("appendToName");
        if (appendToNameNodes.getLength() > 0) {
            appendToName = appendToNameNodes.item(0).getTextContent();
        }

        NodeList prefixNodes = entryElement.getElementsByTagName("prefix");
        if (prefixNodes.getLength() > 0) {
            prefix = prefixNodes.item(0).getTextContent();
        }

        NodeList nameNodes = entryElement.getElementsByTagName("name");
        if (nameNodes.getLength() > 0) {
            nameOverride = nameNodes.item(0).getTextContent();
        }

        if (entryElement.hasAttribute("filters")) {
            filterIDs = Arrays.asList(entryElement.getAttribute("filters").split("\\s*,\\s*"));
        }

    }

    /**
     * Returns true if the entry is associated with an item, false otherwise
     */
    public boolean hasItem() {
        return itemRef != null && !itemRef.trim().equals("");
    }

    /**
     * Returns true if the entry has an item name override, false otherwise
     */
    public boolean hasNameOverride() {
        return nameOverride != null && !nameOverride.trim().equals("");
    }

    /**
     * Returns true if the entry has an item name appendix, false otherwise
     */
    public boolean hasNameAppendix() {
        return appendToName != null && !appendToName.trim().equals("");
    }

    /**
     * Returns true if the entry has a prefix, false otherwise
     */
    public boolean hasPrefix() {
        return prefix != null && !prefix.trim().equals("");
    }

    /**
     * Returns true if the entry is associated with an item name change, false otherwise
     */
    public boolean hasNameChange() {
        return changeNameID != null && !changeNameID.trim().equals("");
    }
}
