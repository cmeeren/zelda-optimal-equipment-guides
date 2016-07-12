package com.relativisticramblings.zeldaguide.controller;

import com.relativisticramblings.zeldaguide.view.*;

import java.io.File;

/**
 * Controller which initializes and runs the correct guide writer (view).
 */
public class GuideCreator {

    private static final String XML_DIR_IN = "xml";
    private static final String HTML_DIR_OUT = "htmlout";

    public static void main(String[] args) {

        File dir = new File(XML_DIR_IN);
        File[] directoryListing = dir.listFiles();
        for (File file : directoryListing) {

            String xml_file_in = file.getName();

            System.out.println("\n#############################\n" + xml_file_in + "\n#############################\n");

            String basename = xml_file_in.substring(0, xml_file_in.lastIndexOf("."));
            String html_file_out = basename + ".html";

            GuideWriter writer = new HTMLWriter(file.getAbsolutePath());
            writer.writeGuideToFile(HTML_DIR_OUT + File.separator + html_file_out);

        }

    }

}
