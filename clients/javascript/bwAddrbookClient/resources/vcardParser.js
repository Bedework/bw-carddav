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
 * 
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

function cleanUpString (string) {
  // replace one or more backslashes followed by comma with comma
  // replace double-quote with backslash double-quote (escape it)
  // replace one or more backslashes followed by a semi-colon with a semi-colon
  cleanedString = string.replace(/\\+,/g,",").replace(/"/g,'\\"').replace(/\\+;/g,";");
  
  return cleanedString;
}

function isJSONValid (jsObject) {
  if (null == jsObject) {
    return false;
  }
  if ("undefined" == typeof(jsObject)) {
    return false;
  }
  try {
    $.parseJSON(jsObject);
  } catch(e) {
      return false;
  }
  return true;
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
  for (var i=0;i<lines.length;i++) {
    var linebuffer = lines[i];
    //each line is in the form of a key[;param;param]:value.  Sometimes the value contains colons, too.
    if (linebuffer != "") {
      while (i + 1 < lines.length) {
        //append continuation lines (lines that start with a space character)
        var rawline = lines[i + 1];
        var leadingSpace = rawline.match(/^ /);
        if (leadingSpace != null) {
          //append this line and avoid processing it the next time through the loop
          linebuffer += jQuery.trim(rawline);
          i++;
        } else {
          //if the next line doesn't begin with space, move on.
          break;
        }
      }
      
      //This section copes with the case when we have a colon character 
      //between double-quotes prior to the colon we should be splitting on.
      //If an uneven number of double-quotes is found on the left side on the 
      //original colon split, we assume the original split was incorrect and move 
      //part of the left (up to the next double-colon) over to the left side.  
      //We'll do this up to five times.  Limiting the attempts prevents
      //an infinite loop caused by a bad vcard with unmatched parentheses.
      //
      
      var colonSplit = linebuffer.split(/:(.+)/);
      var quotes = colonSplit[0].match(/"/g);
      var tempColonSplit;
      var j = 0;
      while (quotes != null && quotes.length % 2 == 1) {
        tempColonSplit = colonSplit[1].split(/:(.+)/);
        colonSplit[0] += ':' + tempColonSplit[0];
        if (tempColonSplit.length > 1) {
          colonSplit[1] = tempColonSplit[1];
        }
        quotes = colonSplit[0].match(/"/g);
        // a little insurance, in case we have unbalanced quotes
        j++;
        if (j > 5) break;
      }
      
      //split out the key and the parameters
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
        bwJsonObj += '"parameter-name": "' + cleanUpString(equalsSplit[0]) + '",'
        bwJsonObj += '"parameter-value": "' + cleanUpString(equalsSplit[1]) + '"'

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
            tmpString = '"' + cleanUpString(colonSplit[1]);
            bwJsonObj += jQuery.trim(tmpString);
          }
          bwJsonObj += '"}';
      }
      if (attributeType == 2) {

        //multiple named values

        var attributeFieldValues = colonSplit[1].split(';');
        bwJsonObj += '"values": {';
        //one array goes from 1 to length-1 and the other from 0 to length-1. Hope it's clear.
        for (y=1;y<attributeInfo.length;y++) {
          bwJsonObj += '"' + cleanUpString(attributeInfo[y]) + '" : ';
          if (y<=attributeFieldValues.length) {
            tmpString = '"' + cleanUpString(attributeFieldValues[y-1]);
            bwJsonObj += jQuery.trim(tmpString) + '"';  
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
  
  if(isJSONValid(bwJsonObj)) {
    vcardsArray.push(bwJsonObj);
  }
}

// return an array of vcards from a list of vcards
function separateIntoCards(data) {
  var vcards = new Array();
  var lines = data.split('\n');
  var buffer = "";
  var vcardsIndex = 0; 
  for (var i=0;i<lines.length;i++) {
    var line = $.trim(lines[i]);
    switch (line) {
      case 'BEGIN:VCARD':
         buffer = line + '\n'; 
         break;
      case 'END:VCARD':
         buffer += line + '\n';
         vcards[vcardsIndex] = buffer;
         vcardsIndex++;
         break;
      default:
         buffer += line + '\n';
    }
  }
  return vcards;
}