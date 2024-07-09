package com.rewyndr.rewyndr.utility;

import android.webkit.MimeTypeMap;

public class FileUtility {
    public static String getMimeType(String filePath) {
        // Ensure that path does not have any spaces
        String strippedFilePath = filePath.replaceAll(" ", "");
        String type = null;
        String extension = "";

        extension = MimeTypeMap.getFileExtensionFromUrl(strippedFilePath);

        if(!extension.isEmpty()) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
