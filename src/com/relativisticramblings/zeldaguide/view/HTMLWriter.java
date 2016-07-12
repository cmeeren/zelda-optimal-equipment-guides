package com.relativisticramblings.zeldaguide.view;

import com.relativisticramblings.zeldaguide.model.*;

import java.util.*;

/**
 * Writes the guide in HTML format
 */
public class HTMLWriter extends GuideWriter {

    private final static String HTML_CLASS_PREFIX = "zeldaguide-";

    public HTMLWriter(String fileIn) {
        super(fileIn);
    }

    /**
     * Returns the HTML guide.
     */
    public String getGuide() {
        String output = "";

        // FIXME: for local testing, uncomment the below
        /*output += "<!DOCTYPE html>";
        output += "<html>";
        output += "<head>";
        output += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
        output += "<title>foobar</title>";
        output += "<link rel='stylesheet' href='../css-js/zeldaguides.css' type='text/css'>";
        output += "</head>";
        output += "<body>";*/

        output += "<div id='zeldaguide'>\n";

        output += getTopMatter();

        for (GameSegment gameSegment : gameSegments) {
            output += getGameSegmentHTML(gameSegment);
            output += getSummaryHTML();
        }

        output = replaceCrossReferences(output);

        output += getBottomMatter();

        output += "</div>";

        // FIXME: for local testing, uncomment the below
        /*output += "</body>";
        output += "</html>";*/

        return output;
    }

    /**
     * Returns the HTML for one segment of the game/guide
     */
    private String getGameSegmentHTML(GameSegment gameSegment) {
        String output = "";
        output += "<div class='" + HTML_CLASS_PREFIX + "gameSegment'>\n";
        output += "\t<h2 id='{{id}}'>\n\t\t{{heading}}\n\t</h2>\n"
                .replace("{{id}}", HTML_CLASS_PREFIX + gameSegment.id)
                .replace("{{heading}}", gameSegment.heading);
        output += "\t<p class='" + HTML_CLASS_PREFIX + "permalink-top' >" + getSegmentPermalinkHTML(gameSegment) + "&emsp;<a href='#zeldaguide'>Jump to top</a></p>\n";
        output += "\t<ul>\n";
        for (Entry entry : gameSegment.entries) {
            output += getEntryHTML(entry);
        }
        output += "\t</ul>\n";
        output += "</div>\n";
        return output;
    }

    /**
     * Returns the HTML for one entry in a segment
     */
    private String getEntryHTML(Entry entry) {
        String output = "";

        // This is a really ugly hack for supporting subheadings in sections via <entry type="subheading">title</entry>
        if (entry.heading != null) {
            output += "\t</ul>\n";
            output += "\t<h3>" + entry.heading + "</h3>\n";
            output += "\t<ul>\n";
            return output;
        }

        output += getEntryLiHTMLOpen(entry);
        output += getEntryLabelHTMLOpen(entry);

        output += getEntryCheckboxHTML(entry);

        output += getEntryItemHTML(entry);
        output += getEntryDescriptionHTML(entry);
        output += getEntryPermalinkHTML(entry);

        output += getEntryLabelHTMLClose();
        output += getEntryLiHTMLClose();

        return output;
    }

    /**
     * Returns the ID for the checkbox HTML element
     */
    private String getCheckboxID(Entry entry) {
        return getEntryID(entry) + "-checkbox";
    }

    /**
     * Returns the opening part of an entry's li element
     */
    private String getEntryLiHTMLOpen(Entry entry) {
        String classes;

        if (entry.checkbox) {
            classes = HTML_CLASS_PREFIX + "checkbox";
        } else {
            classes = HTML_CLASS_PREFIX + "no-checkbox";
        }

        // get filters
        Set<String> filterIDs = new HashSet<>(entry.filterIDs);
        if (entry.hasItem()) filterIDs.addAll(items.get(entry.itemRef).filterIDs);
        if (!filterIDs.isEmpty()) {
            classes += " " + HTML_CLASS_PREFIX + "filtered";
            for (String filterID : filterIDs) {
                classes += " " + HTML_CLASS_PREFIX + "filter-" + filterID;
            }
        }



        // if the associated item has gameVariations, add that to classes
        if (entry.hasItem() && items.get(entry.itemRef).hasGameVariations()) {
            classes += " " + getGameVariationClasses(items.get(entry.itemRef).gameVariations);
        }

        return "\t\t<li id='{{entryID}}' class='{{classes}}'>\n"
                .replace("{{entryID}}", getEntryID(entry))
                .replace("{{classes}}", classes);
    }

    /**
     * Returns the entry ID
     */
    private String getEntryID(Entry entry) {
        if (guideID != null) {
            return HTML_CLASS_PREFIX + guideID + "-" + entry.id;
        } else {
            return HTML_CLASS_PREFIX + entry.id;
        }
    }

    /**
     * Returns the closing part of an entry's li element
     */
    private String getEntryLiHTMLClose() {
        return "\t\t</li>\n";
    }

    /**
     * Returns the opening part of an entry's label element
     */
    private String getEntryLabelHTMLOpen(Entry entry) {
        return "\t\t\t<label for='{{checkboxID}}'>\n"
                .replace("{{checkboxID}}", getCheckboxID(entry));
    }

    /**
     * Returns the closing part of an entry's label element
     */
    private String getEntryLabelHTMLClose() {
        return "\t\t\t</label>\n";
    }

    /**
     * Returns the HTML for the entry's checkbox
     */
    private String getEntryCheckboxHTML(Entry entry) {
        if (entry.checkbox) {
            return "\t\t\t\t<input id='" + getCheckboxID(entry) + "' type='checkbox'>\n";
        } else {
            return "\t\t\t\t<input id='" + getCheckboxID(entry) + "' type='checkbox' disabled checked>\n";
        }
    }

    /**
     * Returns the permalink HTML for an entry
     */
    private String getEntryPermalinkHTML(Entry entry) {
        String output = "";
        output += ("<a class='" + HTML_CLASS_PREFIX + "permalink' href='#{{entryID}}'>Link</a>\n")
                .replace("{{entryID}}", getEntryID(entry));
        return output;
    }

    /**
     * Returns the permalink HTML for an entry in a cross-reference
     */
    private String getCrossRefPermalinkHTML(Entry entry, String linkText) {
        String output = "";
        output += ("<a class='" + HTML_CLASS_PREFIX + "crossref' href='#{{entryID}}'>" + linkText + "</a>")
                .replace("{{entryID}}", getEntryID(entry));
        return output;
    }

    /**
     * Returns the permalink HTML for a game segment
     */
    private String getSegmentPermalinkHTML(GameSegment gameSegment) {
        String output = "";
        output += "<a href='#{{segmentID}}'>Link</a>"
                .replace("{{segmentID}}", HTML_CLASS_PREFIX + gameSegment.id);
        return output;
    }

    /**
     * Returns the description HTML associated with an entry
     */
    private String getEntryDescriptionHTML(Entry entry) {
        String output = "";
        output += "\t\t\t\t";
        for (String gameVariationIDs : entry.descriptions.keySet()) {

            String description = entry.descriptions.get(gameVariationIDs);

            // add description for this gameVariation to output
            output += "<span class='{{gameVariationClasses}}'>{{description}}&nbsp;</span>"
                    .replace("{{gameVariationClasses}}", getGameVariationClasses(gameVariationIDs))
                    .replace("{{description}}", description);
        }
        return output;
    }

    /**
     * Replaces cross-references with the name of the item associated
     * with the referenced entry and a permalink to the entry.
     */
    private String replaceCrossReferences(String html) {

        Map<String, String> toReplace = getCrossReferences(html);

        for (String entryID : toReplace.keySet()) {
            String itemName = crossReferences.get(entryID);
            Entry referencedEntry = gameSegments.getEntryByID(entryID);
            if (referencedEntry != null) {
                String replaceWith = getCrossRefPermalinkHTML(referencedEntry, itemName);
                html = html.replace(toReplace.get(entryID), replaceWith);
            }
        }

        return html;

    }

    /**
     * Get a string with game variation HTML classes
     * @param gameVariationIDs comma-separated list of game variations, e.g. "foo,bar"
     */
    private String getGameVariationClasses(String gameVariationIDs) {
        String gameVariationClasses = "";
        for (String gameVariationID : gameVariationIDs.split("\\s*,\\s*")) {
            gameVariationClasses += HTML_CLASS_PREFIX + "gameVariation-" + gameVariationID + " ";
        }
        return gameVariationClasses;
    }

    /**
     * Returns the item-related parts of an entry
     */
    private String getEntryItemHTML(Entry entry) {
        // if entry is associated with an item, add item name, count, and name appendix
        String output = "";

        output += getEntryPrefix(entry);

        // if the entry is not associated with an item, we still might have <name> and <appendToName>
        if (!entry.hasItem() && entry.hasNameOverride()) {
            output += "\t\t\t\t<span class='" + HTML_CLASS_PREFIX + "item-name'>{{name}}{{nameAppendix}}:</span> \n"
                    .replace("{{name}}", getItemName(entry))
                    .replace("{{nameAppendix}}", getEntryNameAppendix(entry));

            crossReferences.put(entry.id, getItemName(entry));

            return output;
        }

        if (entry.hasItem()) {
            Item item = items.get(entry.itemRef);

            // update item name if this entry changes it
            if (entry.hasNameChange()) item.updateName(entry.changeNameID);

            output += "\t\t\t\t<span class='" + HTML_CLASS_PREFIX + "item-name'>{{name}}{{count}}{{nameAppendix}}:</span> \n"
                    .replace("{{name}}", getItemName(entry))
                    .replace("{{count}}", getCount(entry))
                    .replace("{{nameAppendix}}", getEntryNameAppendix(entry));

            crossReferences.put(entry.id, getItemName(entry)+getCount(entry));

            // increment item count according to entry and add item to summary list
            incrementItemCount(entry);
            addToSummaryList(item);
        }
        return output;
    }

    /**
     * Returns string to be appended to entry name
     */
    private String getEntryNameAppendix(Entry entry) {
        if (entry.hasNameAppendix()) {
            return " " + entry.appendToName;
        } else {
            return "";
        }
    }

    /**
     * Returns the string specifying the count of items added by this entry (e.g. "#3-#5")
     */
    private String getCount(Entry entry) {

        Item item = items.get(entry.itemRef);

        // return empty string if item is unique
        if (item.unique) return "";

        if (entry.add == 1) {
            return " #" + getItemCountStart(entry);
        } else if (entry.add > 1) {
            return " #" + getItemCountStart(entry) + "&ndash;#" + getItemCountEnd(entry);
        } else {
            // return empty string if nothing is added
            return "";
        }
    }

    /**
     * Returns string to be prepended to entry
     */
    private String getEntryPrefix(Entry entry) {
        if (entry.hasPrefix()) {
            return "<strong>" + entry.prefix + "</strong> ";
        } else {
            return "";
        }
    }

    /**
     * Returns the HTML for a summary reflecting the current state of the items
     */
    private String getSummaryHTML() {
        String output = "";

        output += "<div class='" + HTML_CLASS_PREFIX + "summary'>\n";

        for (SummaryList summaryList : summaryLists) {
            output += getSummaryListHTML(summaryList);
        }

        output += "</div>\n";

        return output;
    }

    /**
     * Returns the HTML for a specific summary list in a summary section.
     */
    private String getSummaryListHTML(SummaryList summaryList) {

        String output = "";

        output += "\t<div class='" + HTML_CLASS_PREFIX + "summary-list'>\n";
        output += "\t\t<h3>" + summaryList.getTitle() + "</h3>\n";
        output += "\t\t<ul>\n";

        for (String itemID : summaryList.getItemIDs()) {

            Item item = items.get(itemID);

            // only add item if there are items or if it should be displayed with zero items
            if (item.count > 0 || item.currentParts > 0 || item.displayInSummaryIfZero) {

                String classes = "";

                if (item.hasGameVariations()) {
                    classes += getGameVariationClasses(item.gameVariations);
                }

                // add base filtered class if we are adding filters below
                if (!summaryList.isUpdated(itemID) || !item.filterIDs.isEmpty()) {
                    classes += " " + HTML_CLASS_PREFIX + "filtered";
                }

                if (!summaryList.isUpdated(itemID)) {
                    classes += " " + HTML_CLASS_PREFIX + "filter-oldSummaryItem";
                }

                for (String filterID : item.filterIDs) {
                    classes += " " + HTML_CLASS_PREFIX + "filter-" + filterID;
                }

                output += "\t\t\t<li class='" + classes + "'>" + getSummaryItemName(item) + "</li>\n";

            }

        }

        output += "\t\t</ul>\n";
        output += "\t</div>\n";

        summaryList.markAllItemsOld();

        return output;
    }

    private String getTopMatter() {
        String output = "";

        output += "<div class='" + HTML_CLASS_PREFIX + "topmatter'>\n";

        // FIXME for local testing, uncomment this and comment the similar part below
        /*output += "\t<p style='display: none;'>" +
                "<script src='http://code.jquery.com/jquery-2.2.2.min.js'></script>" +
                "<script src='https://cdn.jsdelivr.net/simplestorage/0.2.1/simpleStorage.min.js'></script>" +
                "<script src='../css-js/zeldaguides.js'></script>" +
                "</p>\n";*/
        output += "\t<p style='display: none;'>" +
                "<script src='/js.cookie-2.1.0.min.js'></script>" +
                "<script src='https://cdn.jsdelivr.net/simplestorage/0.2.1/simpleStorage.min.js'></script>" +
                "<script src='/zeldaguides.js'></script>" +
                "</p>\n";

        output += getIntroHTML();

        output += getJumpLinks();

        output += "\t<div class='" + HTML_CLASS_PREFIX + "settings'>\n";
        output += "\t\t<h3>Settings</h3>\n";
        output += getGameVariationSelectorHTML();
        output += getFilterControlHTML();
        output += getMaintenanceHTML();
        output += "\t</div>\n";

        output += "</div>\n";

        return output;
    }

    private String getIntroHTML() {
        String output = "";

        output += "\t<div class='" + HTML_CLASS_PREFIX + "intro'>\n";

        output += "\t\t<p>This is a no-nonsense, mobile-friendly guide for getting everything as soon as possible and in an efficient order. For description, more guides and feedback, see <a href='/the-legend-of-zelda-optimal-equipment-guides'>The Legend of Zelda optimal equipment guides</a>.</p>\n";

        if (guideDescription != null) {
            output += "\t\t<p>" + guideDescription + "</p>\n";
        }

        output += "\t\t<p>All settings and checkboxes are saved/loaded automatically in your browser.</p>\n";

        output += "\t</div>\n";

        return output;
    }

    private String getJumpLinks() {
        String output = "";

        output += "\t<p><a id='" + HTML_CLASS_PREFIX + "jumpFirstUnchecked' href='#'>&raquo; Jump to first unchecked element</a></p>\n";
        output += "\t<p><a id='" + HTML_CLASS_PREFIX + "jumpLastChecked' href='#'>&raquo; Jump to last checked element</a></p>\n";

        return output;
    }

    private String getGameVariationSelectorHTML() {
        String output = "";

        if (!gameVariations.isEmpty()) {
            output += "\t\t<p class='" + HTML_CLASS_PREFIX + "settingsLabel'>Game variation:</p>\n";
            output += "\t\t<select id='" + HTML_CLASS_PREFIX + "gameSelector'>\n";
            for (GameVariation gameVariation : gameVariations) {
                output += "\t\t\t<option value='{{id}}'>{{title}}</option>\n"
                        .replace("{{id}}", gameVariation.id)
                        .replace("{{title}}", gameVariation.title);
            }
            output += "\t\t</select>\n";
        }

        return output;
    }

    private String getFilterControlHTML() {
        String output = "";

        output += "\t\t<p class='" + HTML_CLASS_PREFIX + "settingsLabel'>Hide:</p>\n";
        output += "\t\t<ul id='" + HTML_CLASS_PREFIX + "filtercontrols'>\n";

        // filters defined in the XML file
        for (Filter filter : filters) {
            output += ("\t\t\t<li class='" + HTML_CLASS_PREFIX + "checkbox'>\n" +
                    "\t\t\t\t<label for='{{checkboxID}}'>\n" +
                    "\t\t\t\t\t<input id='{{checkboxID}}' type='checkbox' name={{name}}>\n" +
                    "\t\t\t\t\t{{title}}\n" +
                    "\t\t\t\t</label>\n" +
                    "\t\t\t</li>\n")
                    .replace("{{checkboxID}}", HTML_CLASS_PREFIX + "filterControl-" + filter.id)
                    .replace("{{name}}", filter.id)
                    .replace("{{title}}", filter.title);
        }

        output += "\t\t</ul>\n";

        return output;
    }

    private String getMaintenanceHTML() {
        String output = "";

        output += "\t\t<p class='" + HTML_CLASS_PREFIX + "settingsLabel'>Maintenance:</p>\n";
        output += "\t\t<button id='" + HTML_CLASS_PREFIX + "uncheckAll'>Uncheck all</button> <button id='" + HTML_CLASS_PREFIX + "checkAll'>Check all</button>\n";

        return output;
    }

    private String getBottomMatter() {
        String output = "";

        output += "<div class='zeldaguide-footer'>\n";
        output += "\t<p>Did you enjoy this guide? <a href='/the-legend-of-zelda-optimal-equipment-guides'>Please leave a comment here</a> to show your appreciation!</p>\n";
        output += "</div>\n";

        return output;
    }

}
