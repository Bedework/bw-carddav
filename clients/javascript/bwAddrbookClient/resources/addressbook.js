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
  
  myLayout = $('body').layout({

    //  enable showOverflow on west-pane so popups will overlap north pane
      west__showOverflowOnHover: true

    //  reference only - these options are NOT required because are already the 'default'
    , closable:       true  // pane can open & close
    , resizable:        true  // when open, pane can be resized 
    , slidable:       true  // when closed, pane can 'slide' open over other panes - closes on mouse-out

    //  some resizing/toggling settings
    , north_resizable:    false // no can do.
    , north__slidable:    false // OVERRIDE the pane-default of 'slidable=true'
    , north__togglerLength_closed: '100%' // toggle-button is full-width of resizer-bar
    , north__spacing_closed:  20    // big resizer-bar when open (zero height)
    , south__resizable:   false // OVERRIDE the pane-default of 'resizable=true'
    //  some pane-size settings
    , west__minSize:      150
  });


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
    $.ajax({
      type: "report",
      url: addrBookUrl,
      dataType: "xml",
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("X-HTTP-Method-Override", "REPORT");
        xhrobj.setRequestHeader("Depth", "1");
        xhrobj.setRequestHeader("Authorization", "Basic");
      },
      success: function(responseData){
        alert(responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });

  $("#add").click(function() {
    //var vcData = "BEGIN:VCARD\nFN:Venerable Bede\nUID:myveryhandcodeduid@mysite.edu6754841\nCLASS:PRIVATE\nCATEGORIES:Services\nREV:20100211T041750Z\nEMAIL;TYPE=PREF;TYPE=INTERNET:vbede@mysite.edu\nTEL;TYPE=HOME:+1 555 555-5555\nADR;TYPE=HOME:;;23 Toadstool Ln;Troy;NY;12180;\nN:Bede;Venerable;;;\nVERSION:4.0\nEND:VCARD";
    //alert($("#FN").val() + "\n" + $("#UID").val() + "\n" + $("#EMAIL").val() + "\n" + $("#HOMEPHONE").val() + "\n" + $("#ADRHOMESTREET").val());
    
    var addrBookUrl = carddavUrl + userpath + $("#userid").val() + bookName;
    var newUUID = "BwABC-" + Math.uuid();
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "FN:" + $("#FIRSTNAME").val() + " " + $("#LASTNAME").val() + "\n";
    vcData += "UID:" + newUUID + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:20100914T041750Z\n";
    vcData += "EMAIL;TYPE=PREF;TYPE=INTERNET:" + $("#EMAIL").val() + "\n";
    vcData += "TEL;TYPE=HOME:" + $("#HOMEPHONE").val() + "\n";  
    vcData += "ADR;TYPE=HOME:;;" + $("#ADRHOMESTREET").val() + ";" + $("#ADRHOMECITY").val() + ";" +  $("#ADRHOMESTATE").val() + ";" + $("#ADRHOMEPOSTAL").val() + ";\n";
    vcData += "N:" + $("#LASTNAME").val() + ";" + $("#FIRSTNAME").val() + ";;;\n"; 
    vcData += "VERSION:4.0\nEND:VCARD";
        
    $.ajax({
      type: "put",
      url: addrBookUrl + newUUID + ".vcf",
      data: vcData,
      dataType: "text",
      processData: false,
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("X-HTTP-Method-Override", "PUT");
        xhrobj.setRequestHeader("If-None-Match", "*");
        xhrobj.setRequestHeader("Authorization", "Basic");
        xhrobj.setRequestHeader("Content-Type", "text/vcard");
      },
      success: function(responseData){
        alert(responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });


  $("#delete").click(function() {
    var addrBookUrl = carddavUrl + userpath + $("#userid").val() + bookName;
    $.ajax({
      type: "delete",
      url: addrBookUrl + $("#DUID").val() + ".vcf",
      dataType: "text",
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("X-HTTP-Method-Override", "DELETE");
        xhrobj.setRequestHeader("Authorization", "Basic");
      },
      success: function(responseData){
        alert(responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });       
  
});