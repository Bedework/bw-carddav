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
  var userid = "";
  
  /****************************
   * SETUP THE DEFAULT STATE:
   ****************************/ 
  
  // Get the user from the query string.
  // The user id will be used for queries against the address book 
  // and for display.
  var qsParameters = {};
  (function () {
      var e,
          d = function (s) { return decodeURIComponent(s.replace(/\+/g, " ")); },
          q = window.location.search.substring(1),
          r = /([^&=]+)=?([^&]*)/g;

      while (e = r.exec(q)) {
        qsParameters[d(e[1])] = d(e[2]);
      }
  })();
  userid = qsParameters.user;
  // display the userid at the root of the personal address book tree
  $("#mainUserBook").html(userid);
  
  
  // Create the three-panel layout
  myLayout = $('body').layout({
    //  enable showOverflow on north-pane so popups will overlap west pane
      north__showOverflowOnHover: true

    //  some resizing/toggling settings
    , north__closable:     false // OVERRIDE the pane-default of closable: true
    , north__resizable:    false // OVERRIDE the pane-default of resizable: true
    , north__slidable:    false // OVERRIDE the pane-default of slidable: true
    //  some pane-size settings
    , west__minSize:      190
    //, north__minSize:    43 
  });

  // Disable the search/filter button until an entity is selected
  //$("#searchButton").attr('disabled', 'disabled');

  
  /****************************
   * EVENT HANDLERS:
   ****************************/
  
  // send a REPORT query for all data
  $("#report").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + bookName;
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
      },
      success: function(responseData, status){
        alert(status + "\n" + responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });
  
  // show form for adding/editing a new contact
  $("#addContact").click(function() {
    showPage("bw-modContact");
  });
  
  // show form for adding/editing a group
  $("#addGroup").click(function() {
    showPage("bw-modGroup");
  });

  // submit a vcard to the server
  $("#submitContact").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + bookName;
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
        xhrobj.setRequestHeader("Content-Type", "text/vcard");
      },
      success: function(responseData, status){
        alert(status + "\n" + responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });

  // remove a vcard from the address book
  $("#delete").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + bookName;
    $.ajax({
      type: "delete",
      url: addrBookUrl + $("#DUID").val() + ".vcf",
      dataType: "xml",
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("X-HTTP-Method-Override", "DELETE");
      },
      success: function(responseData, status){
        alert(status + "\n" +  + responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });       
  
  /* Testing Features */
  // setting the user is for testing
  $("#setuser").click(function() {
    var newid = $("#userid").val();
    if (newid != "") {
      userid = newid;
      alert("User set to " + userid);
    }
  });
  
  // Click to auth is for testing only
  // In production, the user will likely be already authed.
  // If not, the server will prompt for auth when the report query is sent on first load of the page.
  $("#auth").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + bookName;
    $.ajax({
      type: "get",
      url: addrBookUrl,
      dataType: "html",
      success: function(responseData, status){
        alert(status + "\n" + responseData);            
      },
      error: function(msg) {
        // there was a problem
        alert(msg.statusText);
      }
    });
  });
  
});

/****************************
 * GENERIC FUNCTIONS:
 ****************************/
// display the named page
function showPage(pageId) {
  // first make all pages invisible
  $("#bw-pages li").each(function(index){
    $(this).addClass("invisible");
  });
  $("#"+pageId).removeClass("invisible");
};

function changeClass(id, newClass) {
  var identity = document.getElementById(id);
  if (identity == null) {
    alert("No element with id: " + id + " to set to class: " + newClass);
  }
  identity.className=newClass;
};