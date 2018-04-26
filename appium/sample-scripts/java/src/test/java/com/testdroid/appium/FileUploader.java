package com.testdroid.appium;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.testdroid.api.http.MultipartFormDataContent;

public class FileUploader { 
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    protected static Logger logger = LoggerFactory.getLogger(FileUploader.class);
    
    protected static String uploadFile(String targetAppPath, String serverURL, String testdroid_apikey)
            throws IOException {
       //final HttpHeaders headers = new HttpHeaders().setBasicAuthentication("yatneela@in.ibm.com", "Yathuyathu_2$$");
       //final HttpHeaders headers = new HttpHeaders().setBasicAuthentication("x-api-key", testdroid_apikey);
       final HttpHeaders headers = new HttpHeaders().setBasicAuthentication(testdroid_apikey, "");
        

        logger.debug("targetPath is::"+ targetAppPath);
        logger.debug("serverURL is::"+ serverURL);
        logger.debug("testdroid_apikey is::"+ testdroid_apikey);
        
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
                request.setHeaders(headers);
            }

        });
        
        // Download the .apk file first
        
        String fileURL = targetAppPath;
        String fileName  = FilenameUtils.getName(fileURL);
        
        File currentDirectory = new File(new File("").getAbsolutePath());
		System.out.println(currentDirectory.getCanonicalPath());
		System.out.println("Current running directory is::"+currentDirectory.getAbsolutePath());
		String saveDir = currentDirectory.getAbsolutePath();
        try {
            HttpDownloadUtility.downloadFile(fileURL, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // End
        
        MultipartFormDataContent multipartContent = new MultipartFormDataContent();
        logger.debug("Complete Path is :::"+ saveDir+File.separator+fileName);
        FileContent fileContent = new FileContent("application/octet-stream", new File(saveDir+File.separator+fileName));

        MultipartFormDataContent.Part filePart = new MultipartFormDataContent.Part("file", fileContent);
        multipartContent.addPart(filePart);

        logger.debug("Before posting the request");
        AppiumResponse appiumResponse = null;
        
        try{
        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(serverURL + "/upload"), multipartContent);
        

         appiumResponse = request.execute().parseAs(AppiumResponse.class);
        }
        catch(IOException excep)
        {
        	logger.debug("In catch block"+excep.getMessage());
        	throw new IOException(excep);
        }
        
        logger.debug("After posting the request");
        logger.debug(appiumResponse.toString());

        logger.debug("response: " + appiumResponse.uploadStatus.message);

        logger.debug("File id: " + appiumResponse.uploadStatus.fileInfo.file);

        return appiumResponse.uploadStatus.fileInfo.file;

    }
    
    public static class AppiumResponse {
        Integer status;
        @Key("sessionId")
        String sessionId;

        @Key("value")
        FileUploader.UploadStatus uploadStatus;

    }

    public static class UploadedFile {
        @Key("file")
        String file;
    }

    public static class UploadStatus {
        @Key("message")
        String message;
        @Key("uploadCount")
        Integer uploadCount;
        @Key("expiresIn")
        Integer expiresIn;
        @Key("uploads")
        FileUploader.UploadedFile fileInfo;
    }
}
