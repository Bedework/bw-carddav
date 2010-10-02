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


/** Bedework Address Book configuration settings
 *
 * @author Arlen Johnson       johnsa - rpi.edu
 */

/* Define address books for inclusion in the client.
 * Note that when vcard subscriptions are supported, they will 
 * appear dynamically.  This listing allows us to specify
 * the user carddav path and any other public paths we
 * want present for all users.
 * 
 * carddavUrl:    String - root of the carddav server; may be a full
 *                         URL and may include first part of the path
 *                         information (e.g. the context, as we do) 
 * path:          String - more path information
 * bookName:      String - last part of the path information.  If we
 *                         are using a personal book the userid will
 *                         be placed between the path and bookname.
 * personal:      Boolean - if this is a personal address book.
 *                         For the present this client assumes books 
 *                         that are not personal are read-only. 
 * default:       Boolean - if this is the default address book.
 *                         There should be only one of these, but
 *                         if more than one is defined, the client
 *                         will take the first in the array.                        
 * label:         String - the display title for the book
 * vcards:        Array  - an empty array; this will be filled with
 *                         vcard objects when the client connects to 
 *                         the server on page load.
 */
var bwBooks = [
   {
       "carddavUrl" : "/ucarddav",
       "path" : "/user/",
       "bookName" : "/addressbook/",
       "personal" : true,
       "default" : true,
       "label" : "personal",
       "vcards" : [
           
       ] 
   },
   {
       "carddavUrl" : "/ucarddav",
       "path" : "/public",
       "bookName" : "/people/",
       "personal" : false,
       "default" : false,
       "label" : "public people",
       "vcards" : [
             
       ] 
   },
   {
       "carddavUrl" : "/ucarddav",
       "path" : "/public",
       "bookName" : "/locations/",
       "personal" : false,
       "default" : false,
       "label" : "public locations",
       "vcards" : [
             
       ] 
   }
   
 ];

/*  
 * If we find we need to load js language files, 
 * the following method seems to work well.
 * We also need only translate the index.html
 * file.  We should keep a copy of translations in the
 * repository as they are made available.  
 */

// Define the language file to be used in the client
var langfile = "config/lang/en_US.js";

// load the language file dynamically so we 
// need only change the langfile variable to 
// load the correct language strings
$.ajax({
  async: false,
  type: "GET",
  url: langfile,
  data: null,
  dataType: 'script'
});
