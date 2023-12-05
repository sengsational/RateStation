package com.sengsational.ratestation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.entity.BasicHttpEntity;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Used in TopLevelActivity.doInBackground(), but this could change to a logon activity.
 *
 * Created by Owner on 5/13/2016.
 */
public class LoadDataHelper {
    private static LoadDataHelper loadDataHelper;

    public static LoadDataHelper getInstance() {
        if (loadDataHelper == null){
            loadDataHelper = new LoadDataHelper();
        }
        return loadDataHelper;
    }

    private LoadDataHelper() {
    }

    static String getPageContent(String url, HttpResponse unusedParameter, CloseableHttpClient httpclient, BasicCookieStore cookieStore) throws Exception {
        return getPageContent(url, unusedParameter, httpclient, cookieStore, 25);
    }

    // GO TO THE LOGIN PAGE AND COLLECT FORM FIELDS
    static String getPageContent(String url, HttpResponse unusedParameter, CloseableHttpClient httpclient, BasicCookieStore cookieStore, int timeoutSeconds) throws Exception {
        StringBuffer result = null;

        CloseableHttpResponse response = null;

        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        Log.v("sengsational", "Sending 'GET' request to URL : " + url); // Run Order #06
        Log.v("sengsational", "httpclient " + httpclient); // Run Order #07
        if (httpclient != null) {
            int CONNECTION_TIMEOUT_MS = timeoutSeconds * 1000; // Timeout in millis.
            try {
                //Log.v("sengsational", "enable log on httpclient " + httpclient.log.isDebugEnabled()); // Run Order #08
                Log.v("sengsational", "request was null? " + (request == null)); // Run Order #09

                //DRS 2016 06 24 - try new method since it hangs on the white phone (Android version 4.4.2)
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
                        .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                        .setSocketTimeout(CONNECTION_TIMEOUT_MS)
                        .build();

                request.setConfig(requestConfig);
                // DRS 2016 06 24 - END

                response = httpclient.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();

                Log.v("sengsational", "Response Code : [" + responseCode + "] <<<< 200 expected"); // Run Order #10 (200 expected)
                Log.v("sengsational", "Response Status Line : " + response.getStatusLine()); // Run Order #11

                result = getResultBuffer(response); //<<<<<<<<<Pull from response to get the page contents

                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    Log.v("sengsational", "No cookies! !"); // Run Order #13
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        Log.v("sengsational", "- " + cookies.get(i).toString());
                    }
                }
            } catch (Exception e) {
                Log.e("sengsational", "Something bad in getPageContent " + url + ": " + e.getMessage());
                e.printStackTrace();
                result = null;
                if (e.getMessage().contains("timed out")) {
                    throw new Exception(e.getMessage() + " after " + (CONNECTION_TIMEOUT_MS/1000) + " seconds. Try again.");
                }
            }
        }

        if (result != null) {
            return result.toString();
        }
        Log.v("sengsational", "no result from getPageContent " + url);
        return null;
    }

    public static Bitmap getImageContent(String imageUrl, Object o, CloseableHttpClient httpclient, BasicCookieStore cookieStore) {
        Bitmap bitmapResult = null;

        CloseableHttpResponse response = null;

        HttpGet request = new HttpGet(imageUrl);

        request.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        Log.v("sengsational", "Sending 'GET' request to URL : " + imageUrl); // Run Order #06
        Log.v("sengsational", "httpclient " + httpclient); // Run Order #07
        if (httpclient != null) {
            try {
                Log.v("sengsational", "request was null? " + (request == null)); // Run Order #09

                int CONNECTION_TIMEOUT_MS = 25 * 1000; // Timeout in millis.
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
                        .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                        .setSocketTimeout(CONNECTION_TIMEOUT_MS)
                        .build();

                request.setConfig(requestConfig);

                response = httpclient.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();
                Log.v("sengsational", "Response Code : [" + responseCode + "] <<<< 200 expected"); // Run Order #10 (200 expected)
                Log.v("sengsational", "Response Status Line : " + response.getStatusLine()); // Run Order #11


                bitmapResult = getBitmapFromEntity(response); //<<<<<<<<<Pull from response to get the page contents

                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    Log.v("sengsational", "No cookies! !"); // Run Order #13
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        Log.v("sengsational", "- " + cookies.get(i).toString());
                    }
                }
            } catch (Exception e) {
                Log.e("sengsational", "Something bad in getImageContent " + imageUrl + ": " + e.getMessage());
                e.printStackTrace();
                bitmapResult = null;
            }
        }

        if (bitmapResult != null) {
            return bitmapResult;
        }
        Log.v("sengsational", "no bitmapResult from getImageContent " + imageUrl);
        return null;
    }

    private static Bitmap getBitmapFromEntity(CloseableHttpResponse response) {
        Bitmap resultBitmap = null;
        try {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            resultBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.v("sengsational", "Failed to transform entity into bitmap. " + e.getMessage() );
        }
        return resultBitmap;
    }

    /*
    List<NameValuePair> getKioskFormParams(String html, String cardNumber, String cardPin, String mou, String storeNumber) throws UnsupportedEncodingException {

        Log.v("sengsational", "Extracting kiosk form's data..."); // Run Order #15

        Document doc = Jsoup.parse(html);

        List <Element> formList = doc.getElementsByTag("form");

        if (formList.size() == 0) {
            Log.v("sengsational", "ERROR: Unable to get kiosk login form");
            return null;
        }
        Element signinPinForm = formList.get(0);
        Elements inputElements = signinPinForm.getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("homestore", storeNumber));
        Log.v("sengsational", "parmList.add-- key: " + "homestore" + " value: " + storeNumber); // Run Order #16

        // Loop through all <input> elements, overwriting the value if needed
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");
            Log.v("sengsational", "key [" + key + "]   value [" + value + "]");

            if ("cardData".equals(key)) {
                value = "%" + StoreNameHelper.getTwoCharacterStoreIdFromStoreNumber(storeNumber) + cardNumber + "=?";
            } else if ("signinPinNumber".equals(key)) {
                value = cardPin;
            }

            paramList.add(new BasicNameValuePair(key, value));
            Log.v("sengsational", "parmList.add - key: " + key + " value: " + value); // Run Order #16
        }

        return paramList;

    }

     */

    /*********************getPageContent**************************************/

    // PULL FORM FIELDS FROM THE FORM
    List<NameValuePair> getFormParams(String html, String authenticationName, String password, String mou, String storeNumber) throws UnsupportedEncodingException {

        Log.v("sengsational", "Extracting form's data..."); // Run Order #15

        Document doc = Jsoup.parse(html);

        Element loginform = doc.getElementById("custom-login-form");
        Elements inputElements = loginform.getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        boolean mouAdded = false;
        // Loop through all <input> elements, overwriting the value if it's card#, pw, or mou
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");
            Log.v("sengsational", "key [" + key + "]   value [" + value + "]");

            if ("username".equals(key)) {
                value = authenticationName;
            } else if ("password".equals(key)) {
                value = password;
            }

            paramList.add(new BasicNameValuePair(key, value));
            //Log.v("sengsational", "parmList.add - key: " + key + " value: " + value); // Run Order #16
        }

        // Add log in parameter to paramList
        paramList.add(new BasicNameValuePair("op", "Log+In"));
        //Log.v("sengsational", "parmList.add - key: " + "op" + " Form value: " + "Log+In");

        // Loop through <select> elements, overwriting the value if it's store#
        Elements selectElements = loginform.getElementsByTag("select");
        for (Element selectElement : selectElements) {
            String key = selectElement.attr("id");
            String value = selectElement.attr("value");
            String fieldName = selectElement.attr("name");

            if (key.startsWith("edit-field-login-store")) {
                Log.v("sengsational", "The id for edit field login store matched and we are setting value to " + storeNumber);
                value = storeNumber;
                if (fieldName != null && fieldName.startsWith("field_login_store")){
                    Log.v("sengsational", "Form input key: " + fieldName + " Form value: " + value);
                } else if (fieldName != null) {
                    Log.v("sengsational", "fieldName was wrong (" + fieldName + "  Fixing it.");
                    fieldName = "field_login_store[und]";
                    Log.v("sengsational", "Form select key: " + fieldName + " Form value: " + value);
                }
            }
            paramList.add(new BasicNameValuePair(fieldName, value));
        }

        // THIS IS THE ONLY PLACE THAT STORE NAMES AND NUMBERS ARE SAVED
        Elements optionElements = loginform.getElementsByTag("option");
        if (optionElements != null && optionElements.size() > 10) {
            String[] names = new String[optionElements.size()];
            String[] numbers = new String[optionElements.size()];
            for (int i = 0; i<optionElements.size(); i++){
                Element optionElement = optionElements.get(i);
                String key = optionElement.attr("value");
                String value = optionElement.text();
                names[i] = value;
                numbers[i] = key;
            }
            //StoreNameHelper.getInstance().reloadFromPageAndSaveToDatabase(names, numbers);
        }

        return paramList;
    } /********getFormParams*********/



    // DRS 20181023 - Added method
    // DRS 20181023 - Added method
    // DRS 20181023 - Added method
    // DRS 20181023 - Added method
    // DRS 20181023 - Added method
    // PULL FORM FIELDS FROM THE FORM
    List<NameValuePair> getReviewFormParams(String html, int stars, String reviewText, String unusedTimestamp, String saucerName, String beerName, String userName) throws UnsupportedEncodingException {

        Log.v("sengsational", "Extracting review form's data..."); // Run Order #15

        Document doc = Jsoup.parse(html);

        Element reviewForm = doc.getElementById("member-s-tasted-brew-node-form");

        Elements inputElements = reviewForm.getElementsByTag("input");
        Elements textAreaElements = reviewForm.getElementsByTag("textarea");
        Elements selectElements = reviewForm.getElementsByTag("select");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();

        // Loop through all <input> elements, overwriting the value if it's one of our inputs
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            //if (key.startsWith("changed"))
            //    value = timestamp;
            if (key.startsWith("title"))
                value = saucerName + " - " + beerName + " - " + userName;

            if (key.trim().length() == 0) continue;
            if (key.startsWith("field_brew")) continue;

            paramList.add(new BasicNameValuePair(key, value));
            Log.v("sengsational", "parmList.add - key: " + key + " value: " + value); // Run Order #16
        }

        // Loop through all <textarea> elements, overwriting the value if it's one of our inputs
        for (Element textAreaElement: textAreaElements) {
            String key = textAreaElement.attr("name");
            String value = textAreaElement.attr("value");

            if (key.startsWith("body"))
                value = reviewText;

            paramList.add(new BasicNameValuePair(key, value));
            Log.v("sengsational", "parmList.add - key: " + key + " value: " + value); // Run Order #16
        }

        // Loop through all <select> elements, overwriting the value if it's one of our inputs
        for (Element selectElement: selectElements) {
            String key = selectElement.attr("name");
            String value = selectElement.attr("value");

            if (key.startsWith("field_rate"))
                value = "" + stars * 20;

            paramList.add(new BasicNameValuePair(key, value));
            Log.v("sengsational", "parmList.add - key: " + key + " value: " + value); // Run Order #16
        }

        // Add Save in parameter to paramList
        paramList.add(new BasicNameValuePair("op", "Save"));
        Log.v("sengsational", "parmList.add - key: " + "op" + " value: " + "Save");

        return paramList;
    } /********getReviewFormParams*********/


    // POST THE FORM TO THE SERVER
    HttpResponse sendPost(String url, List<NameValuePair> postParams, CloseableHttpClient httpclient, String postType, BasicCookieStore cookieStore, int timeoutSeconds) throws Exception {

        CloseableHttpResponse response = null;

        // NOT SURE THIS IS THE RIGHT WAY OR THE RIGHT AMOUNT OF TIME. https://github.com/smarek/httpclient-android/issues/24
        //HttpConnectionParams.setSoTimeout(httpclient.getParams(), 20000);

        HttpPost post = new HttpPost(url);

        Header[] headers = post.getAllHeaders();

        for (int i = 0; i < headers.length; i++){
            Log.v("sengsational","Headers Before Adding Mine : " + headers[i]);
        }

        StringBuffer cookieNvp = new StringBuffer();
        List<Cookie> cookies = cookieStore.getCookies();
        if (cookies.isEmpty()) {
            Log.v("sengsational", "No cookies! !"); // Run Order #13
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                String aCookie = cookies.get(i).getName() + "=" + cookies.get(i).getValue() + ";";
                //Log.v("sengsational", "cookie [" + aCookie + "]");
                cookieNvp.append(aCookie);
            }
        }

        // add header
        post.setHeader("Host", "www.beerknurd.com");
        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0");
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Cookie", cookieNvp.toString());
        post.setHeader("Connection", "keep-alive");
        if (postType.equals("logon"))
            post.setHeader("Referer", "http://www.beerknurd.com/user");
        else if (postType.equals("review"))
            post.setHeader("Referer", "https://www.beerknurd.com");
        else if (postType.equals("cardnumber"))
            post.setHeader("Referer", "https://www.beerknurd.com/tapthatapp");
        else {
            Log.v("sengsational", "WARNING: Unknown postType: " + postType);
            post.setHeader("Referer", "http://www.beerknurd.com/user");
        }

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // DRS 20181028 - I don't thing this contentLength stuff works since it seems to hit the catch all (most?) of the time.
        String contentLengthString = "(not defined)";
        try {
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            contentLengthString = post.getFirstHeader("Content-Length").getValue();
            basicHttpEntity.setContentLength(Integer.parseInt(contentLengthString));
            Log.v("sengsational", "set content length to " + contentLengthString);
        } catch (Throwable t) {
            Log.v("sengsational", "unable to set content length. contentLengthString:" + contentLengthString);
        }

        post.setEntity(new UrlEncodedFormEntity(postParams));

        boolean printHeadersForDebug = true;
        if (printHeadersForDebug) {
            Header[] headersInPost = post.getAllHeaders();
            for (Header h: headersInPost) {
                Log.v("sengsational", "header in post request [" + h.getName() +"]=[" + h.getValue() + "]");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(post.getEntity().getContent()));
            String read = null;
            StringBuffer sb = new StringBuffer();
            while((read = br.readLine()) != null) {
                sb.append(read);
            }
            Log.v("sengsational", "entity content: " + sb.toString());
        }

        boolean printCookiesForDebug = true;
        if (printCookiesForDebug) {
            List<Cookie> cookiesList = cookieStore.getCookies();
            if (cookiesList.isEmpty()) {
                Log.v("sengsational", "No cookies! !"); // Run Order #13
            } else {
                for (int i = 0; i < cookiesList.size(); i++) {
                    Log.v("sengsational", "Cookie->" + cookiesList.get(i).toString());
                }
            }
        }

        Log.v("sengsational", "\nSending 'POST' request to URL : " + url);

        int connectionTimeoutMs = timeoutSeconds * 1000;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionTimeoutMs)
                .setConnectTimeout(connectionTimeoutMs)
                .setSocketTimeout(connectionTimeoutMs)
                .build();
        post.setConfig(requestConfig);

        response = httpclient.execute(post); //Blocks and throws exception if it times-out

        int responseCode = response.getStatusLine().getStatusCode();

        Log.v("sengsational","Post parameters : " + postParams); // Run Order #18
        Log.v("sengsational","Response Code : [" + responseCode + "] in sendPost()");
        Log.v("sengsational", "Response Status Line : " + response.getStatusLine());

        if (responseCode == 302) {
            manage302(responseCode, response, postParams, httpclient);
        }

        boolean printEntirePageForDebug = false;
        if (printEntirePageForDebug) {
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String read = null;
            StringBuffer sb = new StringBuffer();
            while((read = br.readLine()) != null) {
                sb.append(read);
            }
            Log.v("sengsational","\n\n\nHTTP\n\n\n" + sb.toString() + "\n\n\nHTTPEND");
        }

        return response;
    }     /****************sendPost****************/

    // POST THE FORM TO THE SERVER
    HttpResponse sendQuizPost(String url, List<NameValuePair> postParams, CloseableHttpClient httpclient) throws Exception {

        CloseableHttpResponse response = null;

        HttpPost post = new HttpPost(url);

        Header[] headers = post.getAllHeaders();

        for (int i = 0; i < headers.length; i++){
            Log.v("sengsational","Headers Before Adding Mine : " + headers[i]);
        }

        // add header
        post.setHeader("Host", "www.saucerknurd.com");
        post.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0");
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Accept-Encoding", "gzip, deflate");
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", "http://www.saucerknurd.com/user");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        post.setEntity(new UrlEncodedFormEntity(postParams));

        Log.v("sengsational", "\nSending 'POST' request to URL : " + url + " with postParams " +  EntityUtils.toString(post.getEntity()));
        response = httpclient.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();

        Log.v("sengsational","Post parameters : " + postParams); // Run Order #18
        Log.v("sengsational","Response Code : [" + responseCode + "] in sendPost()");
        Log.v("sengsational", "Response Status Line : " + response.getStatusLine());

        if (responseCode == 302) {
            manage302(responseCode, response, postParams, httpclient);
        }

        return response;
    }     /****************sendPost****************/


    // GET NEW ARRIVALS (JUST LANDED) FROM STORE PAGE
    public static String[] getNewArrivalsFromPage(String storePage, Context context) {
        Document doc = Jsoup.parse(storePage);
        String[] returnNames = null;
        Elements viewsTables = doc.getElementsByClass("views-table");
        if (viewsTables != null && !viewsTables.isEmpty()){
            Element table = viewsTables.get(0); // The only element named "views-table" has new arrivals
            Element tableBody = table.child(0);
            Elements tableRows = tableBody.children();
            returnNames = new String[tableRows.size()];
            int i = 0;
            for (Element tableRow : tableRows){
                Elements tableCells = tableRow.children();
                //Log.v("sengsational", "did we find it??? " + tableCells.get(0).text());
                returnNames[i++] = tableCells.get(0).text(); // The first cell is the name
            }
        } else {
            Log.v("sengsational", "Failed to find new arrivals");
        }
        try {
            Elements storeMetaDiv = doc.getElementsByClass("store-meta");
            if (storeMetaDiv != null && !storeMetaDiv.isEmpty()) {
                String uberEatsLink = "";
                //Elements anchors = storeMetaDiv.get(0).getElementsByTag("a");
                Elements links = storeMetaDiv.select("a[href]");
                if (links != null && !links.isEmpty()) {
                    Log.v("sengsational", ">>>>>>>>> TAGS FOUND <<<<<<<<<<<<< " + links.size());
                    for (Element link: links ) {
                        if (link != null && link.attr("href") != null && link.attr("href").contains("ubereatsx")) {
                            Log.v("sengsational link", "[" + link.attr("href") + "]");
                            uberEatsLink = link.attr("href");
                            break;
                        }
                    }
                    if ("".equals(uberEatsLink)) {
                        for (Element link: links ) {
                            if (link != null && link.attr("href") != null && link.attr("href").contains("doordash")) {
                                Log.v("sengsational link", "[" + link.attr("href") + "]");
                                uberEatsLink = link.attr("href");
                                break;
                            }
                        }
                    }
                } else {
                    Log.v("sengsational", ">>>>>>>>> TAGS NOT FOUND <<<<<<<<<<<<< ");

                }
                //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                //editor.putString(TopLevelActivity.UBER_EATS_LINK, uberEatsLink);
                //editor.apply();
            } else {
                Log.v("sengsational", ">>>>>>>>> STORE META NOT FOUND <<<<<<<<<<<<< ");
            }
        } catch (Throwable t) {
            Log.e("sengsational", "ERROR: could not get Uber Eats link. " + t.getMessage());
        }
        return returnNames;
    }

    public static String getCurrentQueuedBeerNamesFromHtml(String currentQueuePage) {
        StringBuilder foundBeerNames = new StringBuilder();
        Document doc = Jsoup.parse(currentQueuePage);
        Element accordionDiv = doc.getElementById("accordion");
        List <Element> cardList = accordionDiv.getElementsByClass("card");
        for (Element card : cardList) {
            try {
                List <Element> collapseList = card.getElementsByClass("collapse");
                List <Element> removeQueueList = collapseList.get(0).getElementsByClass("removeQueuedBrewIcon");
                String brewId = removeQueueList.get(0).attr("brewid");
                Log.v("sengsational", "brewId for deletion [" + brewId + "]");
                foundBeerNames.append(brewId).append(",");
            } catch (Throwable t) {
                Log.v("sengsational", "ERROR: Could not parse accordion element");
            }
        }
        return foundBeerNames.toString();
    }


    // HELPER METHOD TO PUT THE RESULTING PAGE INTO THE FILE SYSTEM
    boolean writeFile(String page, String fileName){
        fileName = "/sdcard/Download/" + fileName;
        File sdCard = Environment.getExternalStorageDirectory();
        fileName = fileName.replace("/sdcard", sdCard.getAbsolutePath());
        Log.v("sengsational", "The file name ended up to be: " + fileName); // Run Order #23
        File tempFile = new File(fileName);
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(fileName);
            outputStream.write(page.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("sengsational", "Could not write the file." + getStackTraceString(e));
        }
        return false;
    } /*****writeFile*****/

    // HELPER METHOD TO TURN THE RESULT INTO A STRING
    static StringBuffer getResultBuffer(HttpResponse response) throws Exception {
        HttpEntity someEntity =  response.getEntity();

        InputStream stream = someEntity.getContent();
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream)); // <<<<<<<<<<<<<<<<<<<<<<This is the only line we need!!

        if(rd == null) throw new Exception("The reader was null in getResultBuffer.");
        Log.v("sengsational", " is stream ready? " + rd.ready()); // Run Order #12, // Run Order #21
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        // DRS 20160624 - Added 2
        someEntity.consumeContent();
        stream.close();
        return result;
    }   /******getResultBuffer() turn a result into a String ****/

    // JUNK METHOD
    void manage302(int responseCode, CloseableHttpResponse response, List<NameValuePair> postParams, CloseableHttpClient httpclient) throws IOException {
        Log.v("sengsational", "THIS METHOD IS NOT EXPECTED TO RUN except with DefaultRedirectStrategy."); // Run Order #19
        Log.v("sengsational", "[" + responseCode + "]==[302]");
        String redirectUrl = response.getHeaders("Location")[0].getValue();
        Log.v("sengsational", "Redirected to : " + redirectUrl);

        Log.v("sengsational", "Creating httpGet with :\n[" + redirectUrl + "?" + URLEncodedUtils.format(postParams, "utf-8") + "]");

        HttpGet httpGet = new HttpGet(redirectUrl + "?" + URLEncodedUtils.format(postParams, "utf-8"));

        httpGet.setHeader("Host", "www.beerknurd.com");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpGet.setHeader("Accept-Language", "en-US,en;q=0.5");
        //httpGet.setHeader("Cookie", getCookies());
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Referer", "http://www.beerknurd.com/user");
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");

        //***********NO PARAMETERS**********************
        //***********NO PARAMETERS**********************
        //***********NO PARAMETERS**********************
        //request.setParams((new UrlEncodedFormEntity(postParams)));
        //***********NO PARAMETERS**********************
        //***********NO PARAMETERS**********************
        //***********NO PARAMETERS**********************

        response = httpclient.execute(httpGet);

        responseCode = response.getStatusLine().getStatusCode();

        Log.v("sengsational", "Response Code : " + responseCode + "if"); // Run Order #20
        Log.v("sengsational", "Response Status Line : " + response.getStatusLine());

    } /****junk method*****/

    // HELPER METHOD FOR DEBUGGING
    String getStackTraceString(Throwable t){
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


}
