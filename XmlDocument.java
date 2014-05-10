package cn.edu.ustc.aaron.common;

import java.util.HashMap;

/**
@author Aaron Chen
Define XML document interface.
*/

public interface XmlDocument {

    /**
    Create XML Document (omitted)
    @param fileName: File path name
    */

    /**
    Parse XML Document (omitted)
    @param fileName: File path name
    */
    public HashMap<String, String> parseXml (String fileName);
}