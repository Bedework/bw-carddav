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


     $(document).ready(function() {

        var carddavUrl = "/ucarddav";
        var userpath = "/user/";
        var bookName = "/addressbook/"; 

        // set on entry:
        $("#visitListing").attr("href",carddavUrl + userpath + $("#userid").val() + bookName);
        // change if the userid is changed:
        $("#userid").change(function() {
          $("#visitListing").attr("href",carddavUrl + userpath + $("#userid").val() + bookName);
        });

        $("#auth").click(function() {
          var addrBookUrl = carddavUrl + userpath + $("#userid").val() + bookName;
          $.ajax({
            type: "get",
            url: addrBookUrl,
            dataType: "html",
            success: function(responseData){
              alert(responseData);            
            },
            error: function(msg) {
              // there was a problem
              alert(msg.statusText);
            }
          });
        });  

        $("#report").click(function() {
          var addrBookUrl = carddavUrl + userpath + $("#userid").val() + bookName;
          var content = '<?xml version="1.0" encoding="utf-8" ?><C:addressbook-query xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:carddav"><D:prop><D:getetag/><C:address-data/></D:prop><C:filter></C:filter></C:addressbook-query>';
          $.ajax({
            type: "post",
            url: addrBookUrl,
            dataType: "xml",
            processData: false,
            data: content,
            beforeSend: function(xhrobj) {
              xhrobj.setRequestHeader("X-HTTP-Method-Override", "REPORT");
              xhrobj.setRequestHeader("Depth", "1");
              xhrobj.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
              xhrobj.setRequestHeader("Authorization", "Basic");
            },
            success: parsexml,
            error: function(msg) {
              // there was a problem
              alert(msg.statusText);
            }
          });
        });
 
 
       function parsexml(xml) {
           $(xml).find("response").each(function() {
              $(this).find("propstat").each(function() {
                 $(this).find("prop").each(function() {
                    $(this).find("[nodeName=C:address-data]").each(function() {
                       parseVCardBlobIntoJson($(this).text());
                    });
                 });
              });
	   });
         }


        function attributeSpecifics (attribute) {
           var returnArray=new Array();
	   //to add a multipart value, stuff 0 into the first array element, then go from there.
           switch (attribute) {
                   case "BEGIN":
                   case "END":
                    returnArray[0]=0;
                    break;
                  case "ADR":
		    returnArray[0]=2;
                    returnArray[1]="po-box";
                    returnArray[2]="extended-address";
                    returnArray[3]="street-address";
                    returnArray[4]="locality";
                    returnArray[5]="state";
                    returnArray[6]="postal-code";
		    returnArray[7]="country";
                    break;
                  case "TEL":
		    returnArray[0]=2;
                    returnArray[1]="number";
		    returnArray[2]="extension";
                    break;
                  case "N":
                    returnArray[0]=2;
                    returnArray[1]="family-name";
                    returnArray[2]="given-names";
                    returnArray[3]="honorific-prefixes";
                    returnArray[4]="honorific-suffixes";
                    break;
                  default: 
                    returnArray[0]=1;
           }
           return returnArray;
         }
	     

        function parseVCardBlobIntoJson(blob) {
           //each line ends in '\n'
            var jsonObj = "{";
            var lines =  blob.split('\n');
            var lastAttributeName = "";
            for (var i=0;i<lines.sort().length;i++) {
              //each line is in the form of a key[;param;param]:value.  Sometimes the value contains colons, too.
              if (lines[i] != "") {
                var colonSplit = lines[i].split(':');

                //split out the key and the paramaters
                var semiColonSplit = colonSplit[0].split(';');
                var attribute = semiColonSplit[0];
                var attributeInfo = new Array();
                attributeInfo = attributeSpecifics(attribute);
                var attributeType = attributeInfo[0];

                // This takes care of the VCARD:BEGIN and VCARD:END.   Should we pitch VERSION, too?
                if (attributeType == 0) {
	          continue;
                }

                if (lastAttributeName == attribute) {
                   //another member of the array
                   jsonObj += '{';
                } else {
                    //new one, so write out its name and open a new array.
                   jsonObj += '"' + attribute + '": [ {';
                }

                //locate any parameters in the key and write out the parameter array
                jsonObj += '"params": [' 
                for (var n=1;n<semiColonSplit.length;n++) {
                    jsonObj += '{';
                    var equalsSplit = semiColonSplit[n].split('=');

		    // THIS ISN'T COMPLETE -- NEED to split on comma, too.
                    jsonObj += '"parameter-name": "' + equalsSplit[0] + '",'
                    jsonObj += '"parameter-value": "' + equalsSplit[1] + '"'
                    jsonObj += '}';
                    
                    //add a comma between parameters (avoid adding at end)
                    if (n != semiColonSplit.length - 1) {
                       jsonObj += ',';
                    }
                }
               
                jsonObj += '],'

                if (attributeType == 1) {

                    //a single value
                
                   jsonObj += '"value": ';

                   //write out part of value before the first colon -- generally all of it.
                   jsonObj += '"' + colonSplit[1]

                   //put back colon(s) and write out what's past the first colon
                   for (k=2;k<colonSplit.length;k++) { 
		    jsonObj += ':' + colonSplit[k];
                  }
		  jsonObj += '"}';
                }
                if (attributeType == 2) {

                  //multiple named values

                  //Will need to deal with the possibility of colons in the individual values.

                  var attributeFieldValues = colonSplit[1].split(';');
                  jsonObj += '"values": [';
                  //one array goes from 1 to length-1 and the other from 0 to length-1. Hope it's clear.
                  for (y=1;y<attributeInfo.length;y++) {
                     jsonObj += '{"' + attributeInfo[y] + '": ';
                     if (y<attributeFieldValues.length) {
	               jsonObj += '"' + attributeFieldValues[y-1]  + '"}';
                     } else {
                       //avoid undefines
                       jsonObj += '""}';
                     }
          
                     //add a comma between fields (avoid adding at end)
                     if (y != attributeInfo.length - 1) {
                       jsonObj += ',';
                     }
		  }
                  jsonObj += ']}';
                  
                }
                if (lastAttributeName == attribute) {
                     jsonObj += ',';
                } else { 
		     jsonObj += ']';
                     //except for last key, value pair, add a comma.
                     if ((i != lines.length - 1) && (lines[i+1] != "")) {
                        jsonObj += ',';
                     }
                }
                lastAttributeName=attribute;
              }   
	      
            }
            jsonObj += "}";
            $("#listOut").append(jsonObj);
            $("#listOut").append("<hr />");
        }
});     
