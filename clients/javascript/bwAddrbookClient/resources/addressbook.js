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
  this.books = new Array(); // our address books, loaded on init
  this.userid = ""; // our default personal user, loaded on init
  this.defPersBookUrl = ""; // URL of our default personal book, built at init
  this.kindToAdd = "individual"; // the vcard KIND of the contact we wish to add
  this.book; // the currently selected book
  this.card; // the currently selected card
  
  this.init = function(bookTemplate,userid) {
    bwAddressBook.books = bookTemplate;
    bwAddressBook.userid = userid;
    
    for(var i=0; i < bwAddressBook.books.length; i++) {
      var book = bwAddressBook.books[i];
      
      // build the address book URL
      var addrBookUrl = book.carddavUrl + book.path;
      if (book.type == "personal-default" || book.type == "personal") {
        // we only need the userid if the book is personal
        addrBookUrl += userid;
      }
      addrBookUrl += book.bookName;
      
      // set the default personal book url
      if (book.type == "personal-default") {
        bwAddressBook.defPersBookUrl = book.carddavUrl + book.path + bwAddressBook.userid + book.bookName;
      } 
      
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
          showError(msg.statusText);
        }
      });
    }
  };
  
  this.buildMenus = function() {
    var personalBooks = "";
    var subscriptions = "";
    
    // iterate over the books and build up the menus
    // We need to iterate over groups within the books and add them as children
    for (var i=0; i < bwAddressBook.books.length; i++) {
      var book = bwAddressBook.books[i];
      var bookId = "bwBook-" + i;
      switch(book.type) {
        case "personal-default" :
          // this is the default book; mark it as such.  We will replace the title with the user's id
          personalBooks += '<li class="bwBook defaultUserBook" id="' + bookId + '"><a href="#" class="selected">' + bwAddressBook.userid + '</a></li>';
          break;
        case "personal" :
          personalBooks += '<li class="bwBook" id="' + bookId + '"><a href="#">' + book.label + '</a></li>';
          break;
        case "subscription" :
          // this is a subscription
          subscriptions += '<li class="bwBook" id="' + bookId + '"><a href="#">' + book.label + '</a></li>';
          break;
        default :
          alert(book.label + ':\n' + bwAbDispBookType + ' "' + book.type + '" ' + bwAbDispUnsupported);
      }
      
      // create the listings
      this.buildList(i);
    }
    
    // check for empty menus
    if (personalBooks == "") {
      personalBooks = '<li class="empty">no books found</li>'; 
    }
    if (subscriptions == "") {
      subscriptions = '<li class="empty">no books found</li>'; 
    }
    
    // write the menus back to the browser
    $("#bwBooks").html(personalBooks);
    $("#bwSubscriptions").html(subscriptions);
    
    $(".defaultUserBook").droppable({ 
      accept: '#bwAddrBookOutputList td.name',  
      drop:   function () { 
        showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
        return false; 
      } 
    }); 

  };
  
  this.buildList = function(bookIndex) {
    var book = new Array();
    var index = bookIndex;
    var listing = "";
    
    // select the current book
    book = bwAddressBook.books[index];
    
    // Create a tabular listing for display - we can use
    // innerHtml here for speed and simplicity.
    // Display strings are set in the language file 
    // specified in config.js
    listing += '<table class="bwAddrBookTable invisible" id="bwAddrBookTable-'+ index +'">';
    listing += "<tr>";
    listing += "<th>" + bwAbDispListName + "</th>"; // always show name
    // test the other fields to see if the config allows us to display them
    if (book.listDisp.phone) {
      listing += "<th>" + bwAbDispListPhone + "</th>";
    }
    if (book.listDisp.email) {
      listing += "<th>" + bwAbDispListEmail + "</th>";
    }
    if (book.listDisp.title) {
      listing += "<th>" + bwAbDispListTitle + "</th>";
    }
    if (book.listDisp.org) {
      listing += "<th>" + bwAbDispListOrg + "</th>";
    }
    if (book.listDisp.url) {
      listing += "<th>" + bwAbDispListUrl + "</th>";
    }
    listing += "</tr>";
    
    // if we have no cards, tell the user
    if (book.vcards.length == 0) {
      listing += '<tr class="none"><td>' +  bwAbDispListNone + '</td><td></td><td></td><td></td><td></td><td></td></tr>'; 
    } else {
    // we have cards: build the list
      for (var i=0; i < book.vcards.length; i++) {
        var curCard = jQuery.parseJSON(book.vcards[i]);
        var rowClass = "";
        if (i%2 == 1) {
          rowClass = "odd"; 
        }
        // determine the kind of vcard - if not available, assume "individual"
        var kind = "individual";
        var kindIcon = "resources/icons/silk/user.png";
        if (curCard.KIND != undefined && curCard.KIND[0].value != "") {
          kind = curCard.KIND[0].value;
          switch(kind) {
            case "group" :  
              kindIcon = "resources/icons/silk/group.png";
              break;
            case "location" :
              kindIcon = "resources/icons/silk/building.png";
              break;
          }
        }
        
        // check for the existence of the properties
        var fn ="";
        if(curCard.FN != undefined) { 
          fn = curCard.FN[0].value; 
        }
        var tel ="";
        if(curCard.TEL != undefined) { 
          tel = curCard.TEL[0].values[0].number; 
        }
        var email ="";
        if(curCard.EMAIL != undefined) { 
          email = curCard.EMAIL[0].value; 
        }
        var title = "";
        if(curCard.TITLE != undefined) { 
          title = curCard.TITLE[0].value; 
        }
        var org = "";
        if(curCard.ORG != undefined) { 
          org = curCard.ORG[0].values[0].organization_name; 
        }
        var url = "";
        if(curCard.URL != undefined) { 
          url = curCard.URL[0].value; 
        }
        
        listing += '<tr id="bwBookRow-' + index + '-' + i + '" class="' + rowClass + '">'
        listing += '<td id="bwBookName-' + index + '-' + i + '" class="name"><img src="' + kindIcon + '" width="16" height="16" alt="' + kind + '"/>';
        listing += fn + '</td>';
        if (book.listDisp.phone) {
          listing += '<td>' + tel + /*'<span class="typeNote">(kind)</span>' + */ '</td>';
        }
        if (book.listDisp.email) {
          listing += '<td><a href="mailto:' + email + '">' + email + '</a></td>';
        }
        if (book.listDisp.title) {
          listing += '<td>' + title + '</td>';
        }
        if (book.listDisp.org) {
          listing += '<td>' + org + '</td>';
        }
        if (book.listDisp.url) {
          listing += '<td><a href="' + url + '">' + url + '</a></td>';
        }
        listing += "</tr>"
      }
    }
    listing += "</table>"
      
    // add the output to the page
    $("#bwAddrBookOutputList").append(listing);
    
    // make the list items draggable
    $("#bwAddrBookOutputList td.name").draggable({ 
      opacity: 0.5,
      // create a clone & append it to 'body' 
      helper: function (e,ui) {
         return $(this).clone().appendTo('body').css('zIndex',5).show();
      } 
    }) 

  };
   
  this.addContact = function() {
    // For now, we'll assume there is only one book to which we can write.
    // In the future, we'll want to check to see if there is more than 
    // one personal book, and check which is selected.
    var addrBookUrl = bwAddressBook.defPersBookUrl;
    
    // Create the UUID
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
    vcData += "KIND:" + bwAddressBook.kindToAdd + "\n";
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
        var serverMsg = "\n" + status + ": " + responseData;
        showMessage(bwAbDispSuccessTitle,bwAbDispSuccessfulAdd + serverMsg,true);
        clearFields("#addForm");
        window.location.reload(); // this is temporary - for now, just re-fetch the data from the server to redisplay the cards.
      },
      error: function(msg) {
        // there was a problem
        showError(msg.status + " " + msg.statusText);
      }
    });
  };
  
  this.deleteContact = function() {
    // For now, we'll assume there is only one book from which we can delete cards.
    // If we try to delete from another at the moment, we'll either have no access or get  
    // a 404 for not having the uuid in the book
    if(confirm(bwAbDispDeleteConfirm)) {
      // probably want to replace this confirm with a better dialog, but will certainly do for now.
      var addrBookUrl = bwAddressBook.defPersBookUrl;
      var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
      
      $.ajax({
        type: "delete",
        url: addrBookUrl + curCard.UID[0].value + ".vcf",
        dataType: "xml",
        beforeSend: function(xhrobj) {
          xhrobj.setRequestHeader("X-HTTP-Method-Override", "DELETE");
        },
        success: function(responseData, status){
          //alert(status + "\n" +  + responseData);     
          // toss out the card from our local array and from our table
          bwAddressBook.books[bwAddressBook.book].vcards.splice(bwAddressBook.card,1);
          $("#bwBookRow-" + bwAddressBook.book + "-" + bwAddressBook.card).remove();
          showPage("bw-list");
        },
        error: function(msg) {
          // if the message is a 204 No Content, we've actually got the correct response from the server so...
          // treat it like a success:
          if (msg.status == "204") {
            // toss out the card from our local array and from our table
            // need to recolor the table rows...
            bwAddressBook.books[bwAddressBook.book].vcards.splice(bwAddressBook.card,1);
            $("#bwBookRow-" + bwAddressBook.book + "-" + bwAddressBook.card).remove();
            showPage("bw-list");
          } else {
          // there was a problem
            showError(msg.status + " " + msg.statusText);
          }
        }
      });
    }
  }
  
  this.display = function() {
    var index = bwAddressBook.book;
    
    if (index == null) {
      // we have no index; use the personal default book
      for (var i=0; i < bwAddressBook.books.length; i++) {
        if (bwAddressBook.books[i].type = "personal-default") {
          index = i;
          break;
        }
      }
    }
    
    showList(index);
    showPage("bw-list");
  }
  
  this.showDetails = function() {
    var details = "";
    var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    
    // check for the existence of the properties
    var fn = "";
    if(curCard.FN != undefined) { 
      fn = curCard.FN[0].value; 
    }
    var tel = "";
    if(curCard.TEL != undefined) { 
      tel = curCard.TEL[0].values[0].number; 
    }
    var email = "";
    if(curCard.EMAIL != undefined) { 
      email = curCard.EMAIL[0].value; 
    }
    var title = "";
    if(curCard.TITLE != undefined) { 
      title = curCard.TITLE[0].value; 
    }
    var org = "";
    if(curCard.ORG != undefined) { 
      org = curCard.ORG[0].values[0].organization_name; 
    }
    var url = "";
    if(curCard.URL != undefined) { 
      url = curCard.URL[0].value; 
    }
    
    // build the details
    details += '<h1>' + fn + '</h1>';
    details += '<table id="bwDetailsTable">';
    details += '<tr><td class="field">' + bwAbDispDetailsTitle + '</td><td>' + title + '</td></tr>';
    details += '<tr><td class="field">' + bwAbDispDetailsOrg + '</td><td>' + org + '</td></tr>';
    details += '<tr class="newGrouping"><td class="field">' + bwAbDispDetailsPhone + '</td><td>' + tel + '</td></tr>';
    details += '<tr><td class="field">' + bwAbDispDetailsEmail + '</td><a href="mailto:' + email + '">' + email + '</a></td></tr>';
    details += '<tr><td class="field">' + bwAbDispDetailsUrl + '</td><td><a href="' + url + '">' + url + '</a></td></tr>';
    details += '</table>';
    
    $("#bwAddrBookOutputDetails").html(details);
    showPage("bw-details");
  }
  
  // Getters and Setters
  this.setKindToAdd = function(val) {
    bwAddressBook.kindToAdd = val; 
  }
  
  this.setBook = function(val) {
    bwAddressBook.book = val; 
  }
  
  this.setCard = function(val) {
    bwAddressBook.card = val; 
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
  
  // we have a userid, now load the vcards, build the menus, and display the list!
  // bwBooks is defined in addressbookProps.js
  bwAddrBook.init(bwBooks,userid);
  
  // generate the personal and subscribed books menus
  bwAddrBook.buildMenus();
  
  // display the default listing
  bwAddrBook.display();
  
  
  /****************************
   * EVENT HANDLERS:
   ****************************/
  
  // select a book to display
  $(".bwBook").click(function() {
    // extract the book array index from the id
    bwAddrBook.setBook($(this).attr("id").substr($(this).attr("id").indexOf("-")+1));
    
    // remove highlighting from all books
    $(".bwBook a").each(function(index){
      $(this).removeClass("selected");
    });
    // now highlight the one just selected
    $(this).find("a:first-child").addClass("selected");
    
    bwAddrBook.display();
  });

  // display vcard details
  $(".bwAddrBookTable tr").click(function() {
    // get the part of the id that holds the indices
    var indices = $(this).attr("id").substr($(this).attr("id").indexOf("-")+1);
    // extract the book array index from the id
    bwAddrBook.setBook(indices.substr(0,indices.indexOf("-")));
    // extract the item index from the id
    bwAddrBook.setCard(indices.substr(indices.indexOf("-")+1));
    
    bwAddrBook.showDetails();
  });
  
  // show form for adding/editing a new contact
  $("#addContact").click(function() {
    bwAddrBook.setKindToAdd("individual"); // vcard 4 kind
    showPage("bw-modContact");
  });
  
  // show form for adding/editing a group
  $("#addGroup").click(function() {
    bwAddrBook.setKindToAdd("group"); // vcard 4 kind
    showPage("bw-modGroup");
  });
  
  // show form for adding/editing a location
  $("#addLocation").click(function() {
    bwAddrBook.setKindToAdd("location"); // vcard 4 kind
    showPage("bw-modLocation");
  });
  
  // show form for adding/editing a resource (not yet available)
  $("#addResource").click(function() {
    bwAddrBook.setKindToAdd("thing"); // vcard 4 kind
    showPage("bw-modResource");
  });
  
  // submit a vcard to the server
  $("#submitContact").click(function() {
    bwAddrBook.addContact();
  });
  
  // delete a vcard from the address book
  $("#deleteContact").click(function() {
    bwAddrBook.deleteContact();
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
  
  // letter filters 
  $("#filterLetters a").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
  });
  
  // search and filter box
  $("#searchButton").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
  });
  
  // add a set of address fields to the add contact form
  $("#bwAppendAddr").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
  });
  
  // add a new email address field to the add contact form
  $("#bwAppendEmail").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
  });
  
  // add a new phone field to the add contact form
  $("#bwAppendPhone").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
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

//display the named list
function showList(listId) {
  // first make all pages invisible
  $(".bwAddrBookTable").each(function(index){
    $(this).addClass("invisible");
  });
  $("#bwAddrBookTable-"+listId).removeClass("invisible");
};

function changeClass(id, newClass) {
  var identity = document.getElementById(id);
  if (identity == null) {
    alert("No element with id: " + id + " to set to class: " + newClass);
  }
  identity.className=newClass;
};

function clearFields(formId) {
  $(formId + " input").each(function(index){
    $(this).val("");
  });
  $(formId + " textarea").each(function(index){
    $(this).val("");
  });
}

function showError(err) {
  var $dialog = $('<div></div>')
  .html(err)
  .dialog({
    autoOpen: false,
    title: bwAbDispErrorTitle,
    modal: true
  });
  $dialog.dialog('open');
}

function showMessage(title,msg,modality) {
  var $dialog = $('<div></div>')
  .html(msg)
  .dialog({
    autoOpen: false,
    title: title,
    modal: modality
  });
  $dialog.dialog('open');
}

function showConfirm(title,msg) {
  var $dialog = $('<div></div>')
  .html(msg)
  .dialog({
    resizable: false,
    title: title,
    modal: true,
    buttons: {
      "Delete" : function() { // internationalize? I need to pass in the buttons.
        $( this ).dialog( "close" );
        return true;
      },
      "Cancel" : function() {
        $( this ).dialog( "close" );
        return false;
      }
    }
  });
  $dialog.dialog('open');
}

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
