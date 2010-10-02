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

/** Bedework Address Book javascript client functions
 *
 * @author Arlen Johnson       johnsa - rpi.edu
 */

var userid = "";

/****************************
 * ADDRESS BOOK OBJECT:
 ****************************/

// holds the address books and their vcard contents
var bwAddressBook = function() {
  this.books = new Array();
  
  this.init = function(bookTemplate) {
    bwAddressBook.books = bookTemplate;
    for(var i=0; i < bwAddressBook.books.length; i++) {
      var book = bwAddressBook.books[i];
      
      // build the address book URL
      var addrBookUrl = book.carddavUrl + book.path;
      if (book.personal) {
        // we only need the userid if the book is personal
        addrBookUrl += userid;
      }
      addrBookUrl += book.bookName;
      
      // perform a report query on the address book
      var content = '<?xml version="1.0" encoding="utf-8" ?><C:addressbook-query xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:carddav"><D:prop><D:getetag/><C:address-data/></D:prop><C:filter></C:filter></C:addressbook-query>';
      $.ajax({
        type: "post",
        url: addrBookUrl,
        dataType: "xml",
        processData: false,
        async: false,
        data: content,
        beforeSend: function(xhrobj) {
          xhrobj.setRequestHeader("X-HTTP-Method-Override", "REPORT");
          xhrobj.setRequestHeader("Depth", "1");
          xhrobj.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
        },
        success: function(responseData) {
          // send the response and the vcards array to be loaded
          parsexml(responseData,book.vcards);
        },
        error: function(msg) {
          // there was a problem
          alert(msg.statusText);
        }
      });
    }
  };
  
  this.displayList = function(bookIndex) {
    var book = new Array();
    var index = bookIndex;
    var listing = "";
    
    if (index == null) {
      // we have no index; find the default book
      for (var i=0; i < bwAddressBook.books.length; i++) {
        if (bwAddressBook.books[i].default) {
          index = i;
          break;
        }
      }
    }
    
    // select the current book
    book = bwAddressBook.books[index];
    
    // Create a tabular listing for display - we can use
    // innerHtml here for speed and simplicity.
    // Display strings are set in the language file 
    // specified in config.js
    listing += "<table id=\"bwAddrBookTable\">";
    listing += "<tr>";
    listing += "<th>" + bwAbDispListName + "</th>";
    listing += "<th>" + bwAbDispListPhone + "</th>";
    listing += "<th>" + bwAbDispListEmail + "</th>";
    listing += "<th>" + bwAbDispListTitle + "</th>";
    listing += "<th>" + bwAbDispListOrg + "</th>";
    listing += "<th>" + bwAbDispListUrl + "</th>";
    listing += "</tr>";
    for (var i=0; i < book.vcards.length; i++) {
      var curCard = jQuery.parseJSON(book.vcards[i]);
      var rowClass = "";
      if (i%2 == 1) {
        rowClass = "odd"; 
      }
      // determine the kind of vcard - if not available, assume "individual"
      var kind = "individual";
      var kindIcon = "resources/icons/silk/user.png";
      if (curCard.KIND != undefined && curCard.KIND.value != "") {
        kind = curCard.KIND.value;
        switch(kind) {
          case "group" :  
            kindIcon = "resources/icons/silk/group.png";
            break;
          case "location" :
            kindIcon = "resources/icons/silk/building.png";
            break;
        }
      }
      
      // check for the existence of other properties (will need to do all)
      var title = "";
      if(curCard.TITLE != undefined) { 
        title = curCard.TITLE[0].value; 
      }
      var org = "";
      if(curCard.ORG != undefined) { 
        org = curCard.ORG[0].value; 
      }
      var url = "";
      if(curCard.URL != undefined) { 
        url = curCard.URL[0].value; 
      }
      
      listing += "<tr class=\"" + rowClass + "\">"
      listing += "<td><img src=\"" + kindIcon + "\" width=\"16\" height=\"16\" alt=\"" + kind + "\"/>";
      listing += curCard.FN[0].value + "</td>";
      listing += "<td>" + curCard.TEL[0].values[0].number + /*"<span class=\"typeNote\">(kind)</span>" + */ "</td>";
      listing += "<td><a href=\"mailto:" + curCard.EMAIL[0].value + "\">" + curCard.EMAIL[0].value + "</a></td>";
      listing += "<td>" + title + "</td>";
      listing += "<td>" + org + "</td>";
      listing += "<td>" + url + "</td>";
      listing += "</tr>"
    }
    listing += "</table>"
      
    // replace the output and show the page
    $("#bwAddrBookOutputList").html(listing);
    showPage("bw-list");
    
  }
};

$(document).ready(function() {

  var bwAddrBook = new bwAddressBook();
  
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

  /****************************
   * INITIALIZE AND DISPLAY
   ****************************/ 
  
  // we have a userid, now load the vcards!
  // bwBooks is defined in addressbookProps.js
  bwAddrBook.init(bwBooks);
  
  // display the default listing
  bwAddrBook.displayList();
  
  
  /****************************
   * EVENT HANDLERS:
   ****************************/
  
  // send a REPORT query for all data
  // SOON TO BE DEPRECATED - this is just a test function
  var carddavUrl = "/ucarddav";
  var userpath = "/user/";
  var userBookName = "/addressbook/";
  $("#report").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + userBookName;
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
      success: parsexml,
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
  
  //show form for adding/editing a group
  $("#addLocation").click(function() {
    showPage("bw-modLocation");
  });

  $("#mainUserBook").click(function() {
    bwAddrBook.displayList(0);
  });
  
  // submit a vcard to the server
  $("#submitContact").click(function() {
    var addrBookUrl = carddavUrl + userpath + userid + userBookName;
    var newUUID = "BwABC-" + Math.uuid();
    
    // build the revision date
    var now = new Date();
    var revDate = String(now.getUTCFullYear());
        revDate += String(now.getUTCMonthFull());
        revDate += String(now.getUTCDateFull()) + "T";
        revDate += String(now.getUTCHoursFull());
        revDate += String(now.getUTCMinutesFull());
        revDate += String(now.getUTCSecondsFull()) + "Z"; 
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    vcData += "UID:" + newUUID + "\n";
    vcData += "FN:" + $("#FIRSTNAME").val() + " " + $("#LASTNAME").val() + "\n";
    vcData += "N:" + $("#LASTNAME").val() + ";" + $("#FIRSTNAME").val() + ";;;\n";
    vcData += "ORG:" + $("#ORG").val() + ";;\n";
    vcData += "TITLE:" + $("#TITLE").val() + "\n";
    vcData += "NICKNAME:" + $("#NICKNAME").val() + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + revDate + "\n";
    vcData += "EMAIL;TYPE=" + $("#EMAILTYPE-01").val() + ":" + $("#EMAIL").val() + "\n";
    vcData += "TEL;TYPE=" + $("#PHONETYPE-01").val() + ":" + $("#PHONE-01").val() + "\n";  
    vcData += "ADR;TYPE=" + $("#ADDRTYPE-01").val() + ":" + $("#POBOX-01").val() + ";" + $("#EXTADDR-01").val() + ";" + $("#STREET-01").val() + ";" + $("#CITY-01").val() + ";" +  $("#STATE-01").val() + ";" + $("#POSTAL-01").val() + ";" + $("#COUNTRY-01").val() + "\n";
    //vcData += "GEO:TYPE=" + $("#ADDRTYPE-01").val() + ":geo:" + $("#GEO-01").val() + "\n";;
    vcData += "URL:" + $("#WEBPAGE").val() + "\n";
    vcData += "PHOTO:VALUE=uri:" + $("#PHOTOURL").val() + "\n";
    vcData += "NOTE:" + $("#NOTE").val() + "\n";
    vcData += "END:VCARD";
    alert(vcData);
        
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
    var addrBookUrl = carddavUrl + userpath + userid + userBookName;
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
  
  // contextual help
  $(".bwHelpLink").hover(
    function(){
      $(this).find(".bwHelp").css("display","block");
    }, 
    function(){
      $(this).find(".bwHelp").css("display","none");
    }  
  );
  
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
    var addrBookUrl = carddavUrl + userpath + userid + userBookName;
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


/* UTC FORMATTERS */

// return a formatted UTC month, prepended with zero if needed
Date.prototype.getUTCMonthFull = function() {
  var monthFull = this.getUTCMonth() + 1;
  if (monthFull < 10) {
    return "0" + monthFull;
  }  
  return monthFull;
};
// return a formatted UTC day date, prepended with zero if needed
Date.prototype.getUTCDateFull = function() {
  var dateFull = this.getUTCDate();
  if (dateFull < 10) {
    return "0" + dateFull;
  }  
  return dateFull;
};
// return formatted UTC hours, prepended with zero if needed
Date.prototype.getUTCHoursFull = function() {
  var hoursFull = this.getUTCHours();
  if (hoursFull < 10) {
    return "0" + hoursFull;
  }  
  return hoursFull;
};
// return formatted UTC minutes, prepended with zero if needed
Date.prototype.getUTCMinutesFull = function() {
  var minutesFull = this.getUTCMinutes();
  if (minutesFull < 10) {
    return "0" + minutesFull;
  }  
  return minutesFull;
};
//return formatted UTC seconds, prepended with zero if needed
Date.prototype.getUTCSecondsFull = function() {
  var secondsFull = this.getUTCSeconds();
  if (secondsFull < 10) {
    return "0" + secondsFull;
  }  
  return secondsFull;
};
