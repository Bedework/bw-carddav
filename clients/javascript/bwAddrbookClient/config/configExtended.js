/*
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/


/** Bedework Address Book extended configuration settings
 *
 * @author Arlen Johnson       johnsa - bedework.edu
 *
 * This config file demonstrates how to include subscriptions
 * to other carddav accounts in the client.
 * This example includes a subscription to two public
 * accounts that are part of Bedework 3.7.  Such subscriptions are
 * probably not practical in a production environment as they'd
 * likely be too large.
 *
 * To use this config,
 * 1. change the reference to config.js to configExtended.js in the
 *    index.html file
 * 2. uncomment the subscriptions menu div in index.html
 *
 */

/* Define address books for inclusion in the client.
 * We will assume for now that there is only one writable
 * personal address book.  It is indicated by setting
 * "type".  We will likewise assume for now that all
 * subscribed books are read-only.
 *
 * Note that when vcard subscriptions are supported, they will
 * appear dynamically.  This listing allows us to specify
 * the user carddav path and any other public paths we
 * want present for all users.
 *
 * label:         String - the display title for the book
 * carddavUrl:    String - root of the carddav server; may be a full
 *                         URL and may include first part of the path
 *                         information (e.g. the context, as we do)
 * path:          String - more path information
 * bookName:      String - last part of the path information.  If we
 *                         are using a personal book the userid will
 *                         be placed between the path and bookname.
 * type:          String - takes the following values:
 *                         personal-default  - the default personal book: there may be only one
 *                         personal          - another personal book
 *                         subscription      - a subscribed address book
 *                                             For the present this client assumes books
 *                                             that are not personal are read-only.
 * listDisp       Object - list of fields to display (or not) in the address book list
 * detailDisp     Object - list of fields to display (or not) in a contact's detail view
 * vcards:        Array  - an empty array; this will be filled with
 *                         vcard objects when the client connects to
 *                         the server on page load.
 */
var bwBooks = [
   {
       "label" : "personal",
       "carddavUrl" : "/ucarddavweb",
       "path" : "/user/",
       "bookName" : "/addressbook/",
       "type" : "personal-default",
       "listDisp" : {
          "name" : false,
          "familyName" : true,
          "givenNames" : true,
          "phone" : true,
          "phone" : true,
          "email" : true,
          "title" : true,
          "org" : true,
          "url" : true
       },
       "detailDisp" : {
          "name" : false,
          "familyName" : true,
          "givenNames" : true,
          "phone" : true,
          "email" : true,
          "title" : true,
          "org" : true,
          "url" : true
       },
       "vcards" : [

       ]
   },
   {
       "label" : "public people",
       "carddavUrl" : "/ucarddavweb",
       "path" : "/public",
       "bookName" : "/people/",
       "type" : "subscription",
       "listDisp" : {
         "name" : false,
         "familyName" : true,
         "givenNames" : true,
         "phone" : true,
         "email" : true,
         "title" : true,
         "org" : true,
         "url" : true
       },
       "detailDisp" : {
         "phone" : true,
         "email" : true,
         "title" : true,
         "org" : true,
         "url" : true
       },
       "vcards" : [

       ]
   },
   {
       "label" : "public locations",
       "carddavUrl" : "/ucarddavweb",
       "path" : "/public",
       "bookName" : "/locations/",
       "type" : "subscription",
       "listDisp" : {
         "name" : true,
         "familyName" : false,
         "givenNames" : false,
         "phone" : true,
         "email" : false,
         "title" : false,
         "org" : false,
         "url" : true
       },
       "detailDisp" : {
         "name" : true,
         "familyName" : false,
         "givenNames" : false,
         "phone" : true,
         "email" : true,
         "title" : true,
         "org" : true,
         "url" : true
       },
       "vcards" : [

       ]
   }

 ];

/* Define public CardDAV servers on which to allow
 * searches.  These will appear in a drop-down list next
 * to the search input field.  The responses must be in json format
 */
var bwPublicCardDAV = [
   {
     "label" : "public people",
     "url" : "/ucarddavweb/find?format=json&addrbook=/public/people/"
   },
   {
     "label" : "public locations",
     "url" : "/ucarddavweb/find?format=json&addrbook=/public/locations/"
   }
];

/*
 * Load language file for javascript functions.
 * To internationalize, you must also translate
 * the index.html file (or swap in a translated
 * copy).  We will keep a copy of
 * translations in the repository
 * as they are made available.
 */

// Define the language file to be used in the client
// for javascript functions that generate textual output
var langfile = "config/lang/en_US.js";

// load the language file
$.ajax({
  async: false,
  type: "GET",
  url: langfile,
  data: null,
  dataType: 'script'
});
