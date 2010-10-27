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

/** Bedework Address Book vcard parsing functions
 *
 * @author Barry Leibson
 */

function parsexml(xml,vcardsArray) {
  $(xml).find("response").each(function() {
    var href = $(this).find("href").text();
    $(this).find("propstat").each(function() {
      $(this).find("prop").each(function() {
        var etag = $(this).find("getetag").text();
        $(this).find("[nodeName=C:address-data]").each(function() {
          parseVCardBlobIntoJson($(this).text(),vcardsArray,href,etag);
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
      returnArray[1]="po_box";
      returnArray[2]="extended_address";
      returnArray[3]="street_address";
      returnArray[4]="locality";
      returnArray[5]="state";
      returnArray[6]="postal_code";
      returnArray[7]="country";
      break;
    case "TEL":
      returnArray[0]=2;
      returnArray[1]="number";
      returnArray[2]="extension";
      break;
    case "N":
      returnArray[0]=2;
      returnArray[1]="family_name";
      returnArray[2]="given_names";
      returnArray[3]="honorific_prefixes";
      returnArray[4]="honorific_suffixes";
      break;
    case "ORG":
      // ORG's can have any number of organizational-units. We'll make room for 10.
      returnArray[0]=2;
      returnArray[1]="organization_name";
      returnArray[2]="organizational_unit_1";
      returnArray[3]="organizational_unit_2";
      returnArray[4]="organizational_unit_3";
      returnArray[5]="organizational_unit_4";
      returnArray[6]="organizational_unit_5";
      returnArray[7]="organizational_unit_6";
      returnArray[8]="organizational_unit_7";
      returnArray[9]="organizational_unit_8";
      returnArray[10]="organizational_unit_9";
      returnArray[11]="organizational_unit_10";
      break;
    default: 
      returnArray[0]=1;
  }
  return returnArray;
}


function parseVCardBlobIntoJson(blob,vcardsArray,href,etag) {
  //each line ends in '\n'
  var bwJsonObj = "{";
  bwJsonObj += '"href" : "' + href + '",';
  //the etag comes quoted; remember to put the quotes back when writing the headers
  bwJsonObj += '"etag" : ' + etag + ','; 
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

      // This takes care of the VCARD:BEGIN and VCARD:END.
      if (attributeType == 0) {
        continue;
      }

      if (lastAttributeName == attribute) {
        //another member of the array
        bwJsonObj += ',{';
      } else {
	if (lastAttributeName != "") {
          //new one, so close the last array, 
          bwJsonObj += '],';
        }
        // write out attribute name and open a new array.
        bwJsonObj += '"' + attribute + '": [ {';
      }

      //locate any parameters in the key and write out the parameter array
      bwJsonObj += '"params": {' 
      for (var n=1;n<semiColonSplit.length;n++) {
          var equalsSplit = semiColonSplit[n].split('=');

          // THIS ISN'T COMPLETE -- NEED to split on comma, too.
          bwJsonObj += '"parameter-name": "' + equalsSplit[0] + '",'
          bwJsonObj += '"parameter-value": "' + equalsSplit[1] + '"'

          //add a comma between parameters (avoid adding at end)
          if (n != semiColonSplit.length - 1) {
            bwJsonObj += ',';
          }
      }

      bwJsonObj += '},'

      if (attributeType == 1) {

          //a single value

          bwJsonObj += '"value": ';

          if (colonSplit.length == 1) {
            //write out empty string
	    bwJsonObj += '"';
          } else {
            //write out part of value before the first colon -- generally all of it.
            bwJsonObj += '"' + colonSplit[1].replace(/\\,/g,",");
          }

          //put back colon(s) and write out what's past the first colon
          for (k=2;k<colonSplit.length;k++) { 
            bwJsonObj += ':' + colonSplit[k].replace(/\\,/g,",");
          }
          bwJsonObj += '"}';
      }
      if (attributeType == 2) {

        //multiple named values

        //Will need to deal with the possibility of colons in the individual values.

        var attributeFieldValues = colonSplit[1].split(';');
        bwJsonObj += '"values": {';
        //one array goes from 1 to length-1 and the other from 0 to length-1. Hope it's clear.
        for (y=1;y<attributeInfo.length;y++) {
          bwJsonObj += '"' + attributeInfo[y] + '" : ';
          if (y<=attributeFieldValues.length) {
            bwJsonObj += '"' + attributeFieldValues[y-1] + '"';
          } else {
            //avoid undefines
            bwJsonObj += '""';
          }

          //add a comma between fields (avoid adding at end)
          if (y != attributeInfo.length - 1) {
            bwJsonObj += ',';
          }
        }
        bwJsonObj += '}}';

      }
      lastAttributeName=attribute;
    }   
  }
  bwJsonObj += "]}";
  vcardsArray.push(bwJsonObj);
}

