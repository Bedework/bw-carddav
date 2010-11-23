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
 * 
 */

var userid = "";
var bwAddrBook = "";

/****************************
 * ADDRESS BOOK OBJECT:
 ****************************/

// holds the address books and their vcard contents
var bwAddressBook = function() {
  this.books = new Array(); // our address books, loaded on init
  this.userid = ""; // our default personal user, loaded on init
  this.defPersBookUrl = ""; // URL of our default personal book, built at init
  this.creating = true; // are we creating or editing an item?
  this.book; // the currently selected book
  this.card; // the currently selected card
  //this.selectedMenuId = $.cookie("selectedMenuId"); // the currently selected menu item; might be null
  this.groupMenus = new Array(); // a place to store groups when building menus
  
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
          var error = msg.statusText;
          if (msg.status == "404") {
            error = bwAbDispError404;
            bwAddressBook.userid = bwAbDispErrorAccessDenied;
          }
          if (msg.status == "500") {
            error = bwAbDispError500;
          }
          showError(error);
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
      var bookLinkId = "bwBookLink-" + i;
      switch(book.type) {
        case "personal-default" :
          // this is the default book; mark it as such.  We will replace the title with the user's id
          personalBooks += '<li id="' + bookId + '">';
          personalBooks += '<a href="#" class="bwBook bwBookLink defaultUserBook selected" id="' + bookLinkId + '">' + bwAddressBook.userid + '</a>';
          personalBooks += '<ul class="bwGroups"></ul>';
          personalBooks += '</li>';
          break;
        case "personal" :
          personalBooks += '<li id="' + bookId + '"><a href="#" class="bwBook bwBookLink" id="' + bookLinkId + '">' + book.label + '</a></li>';
          break;
        case "subscription" :
          // this is a subscription
          subscriptions += '<li id="' + bookId + '"><a href="#" class="bwSubscription bwBookLink" id="' + bookLinkId + '">' + book.label + '</a></li>';
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
    
    // Got groups? put them with the correct books.
    // We've gathered the group menu items in an array when we built the listing (below).
    for (var j=0; j<this.groupMenus.length; j++) {
      $("#bwBook-" + j + " ul").html(this.groupMenus[j]);
    }

  };
  
  this.buildList = function(bookIndex) {
    var book = new Array();
    var index = bookIndex;
    var listing = ""; // for tabular listing of book members
    var groups = ""; // for listing of groups in the menu
    
    // select the current book
    book = bwAddressBook.books[index];
    
    // Create a tabular listing for display - we can use
    // innerHtml here for speed and simplicity.
    // Display strings are set in the language file 
    // specified in config.js
    listing += '<table class="bwAddrBookTable invisible" id="bwAddrBookTable-'+ index +'">';
    listing += '<thead><tr>';
    // test fields to see if the config allows us to display them
    if (book.listDisp.name) {
      listing += "<th>" + bwAbDispListName + "</th>";
    }
    if (book.listDisp.familyName) {
      listing += "<th>" + bwAbDispListFamilyName + "</th>";
    }
    if (book.listDisp.givenNames) {
      listing += "<th>" + bwAbDispListGivenNames + "</th>";
    }
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
    listing += "</thead><tbody>";
    // if we have no cards, tell the user
    if (book.vcards.length == 0) {
      listing += '<tr class="none"><td>' +  bwAbDispListNone + '</td><td></td><td></td><td></td><td></td><td></td></tr>'; 
    } else {
    // we have cards: build the list
      for (var i=0; i < book.vcards.length; i++) {
        var curCard = jQuery.parseJSON(book.vcards[i]);
        
        // determine the kind of vcard - if not available, assume "individual"
        var kind = "individual";
        var kindIcon = "resources/icons/silk/user.png";
        if (curCard.KIND != undefined && curCard.KIND[0].value != "") {
          kind = curCard.KIND[0].value.stripTags();
          switch(kind) {
            case "group" :
              // NOTE: showing groups in the list is now deprecated,
              // but we will leave this here in case we choose to restore them
              kindIcon = "resources/icons/silk/group.png";
              break;
            case "location" :
              kindIcon = "resources/icons/silk/building.png";
              break;
          }
        }
        
        // get the fn - needed for both groups and other entries
        var fn ="";
        if(curCard.FN != undefined) { 
          fn = curCard.FN[0].value.stripTags(); 
        }
        
        if (kind == "group") { 
          // we have a group: add it to the groups list that belongs in the menu tree
          groups += '<li id="bwBookGroup-' + index + '-' + i + '"><a href="#" id="bwBookGroupLink-' + index + '-' + i + '" class="bwGroup">' + fn + '</a></li>';
          
          // write the groups to an array for later use building the menus
          this.groupMenus[index] = groups;
          
        } else { 
          // we have an individual, location, or thing
          // check for the existence of the remaining properties
          var familyName ="";
          if(curCard.N != undefined) { 
            if(curCard.N[0].values.family_name != undefined) { 
              familyName = curCard.N[0].values.family_name.stripTags(); 
            }
          }
          var givenNames ="";
          if(curCard.N != undefined) { 
            if(curCard.N[0].values.given_names != undefined) { 
              givenNames = curCard.N[0].values.given_names.stripTags(); 
            }
          }
          var tel ="";
          if(curCard.TEL != undefined) { 
            tel = curCard.TEL[0].values.number.stripTags(); 
          }
          var email ="";
          if(curCard.EMAIL != undefined) { 
            email = $ESAPI.encoder().encodeForHTML(curCard.EMAIL[0].value); 
          }
          var title = "";
          if(curCard.TITLE != undefined) { 
            title = curCard.TITLE[0].value.stripTags(); 
          }
          var org = "";
          if(curCard.ORG != undefined) { 
            org = curCard.ORG[0].values.organization_name.stripTags(); 
          }
          var url = "";
          if(curCard.URL != undefined) { 
            url = curCard.URL[0].value.stripTags(); 
          }
          
          listing += '<tr id="bwBookRow-' + index + '-' + i + '">';
          if (book.listDisp.name) {
            listing += '<td class="name" id="bwCardFN-' + index + '-' + i + '">';
            listing += '<img src="' + kindIcon + '" width="16" height="16" alt="' + kind + '"/>' + fn + '</td>';
          }
          if (book.listDisp.familyName) {
            listing += '<td class="name" id="bwCardFamName-' + index + '-' + i + '">';
            listing += '<a name="' + familyName.substr(0,1) + '"></a>';
            listing += '<img src="' + kindIcon + '" width="16" height="16" alt="' + kind + '"/>' + familyName + '</td>';
          }
          if (book.listDisp.givenNames) {
            listing += '<td class="name">' + givenNames + '</td>';
          }
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
          listing += "</tr>";
        }
      }
    }
    listing += "</tbody></table>"
      
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
    
    /* uncomment if we choose to separate icons from the name field
    // make the list items draggable by icon too
    $("#bwAddrBookOutputList td.kind").draggable({ 
      opacity: 0.5,
      // create a clone & append it to 'body' 
      helper: function (e,ui) {
         return $(this).clone().appendTo('body').css('zIndex',5).show();
      } 
    })
    */

  };
   
  // *********************
  // FORM HANDLING
  // *********************
  
  // add an entry to the vcard server
  this.addEntry = function(vcData,newUUID,formId) {
    // For now, we'll assume there is only one book to which we can write.
    // In the future, we'll want to check to see if there is more than 
    // one personal book, and check which is selected.
    var addrBookUrl = bwAddressBook.defPersBookUrl;
    
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
        if (formId) {
          clearFields(formId);
        }
        window.location.reload(); // this is temporary - for now, just re-fetch the data from the server to redisplay the cards.
      },
      error: function(msg) {
        // there was a problem
        showError(msg.status + " " + msg.statusText);
      }
    });
  }
  
  // update an entry on the vcard server
  this.updateEntry = function(vcData,cardHref,cardEtag,formId) {
    $.ajax({
      type: "put",
      url: cardHref,
      data: vcData,
      dataType: "text",
      processData: false,
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("X-HTTP-Method-Override", "PUT");
        xhrobj.setRequestHeader("If-Match", '"' + cardEtag + '"'); // restore the etag dquotes
        xhrobj.setRequestHeader("Content-Type", "text/vcard");
      },
      success: function(responseData, status){
        var serverMsg = "\n" + status + ": " + responseData;
        showMessage(bwAbDispSuccessTitle,bwAbDispSuccessfulUpdate + serverMsg,true);
        if (formId != null) {
          clearFields(formId);
        }
        window.location.reload(); // this is temporary - for now, just re-fetch the data from the server to redisplay the cards.
        
      },
      error: function(msg) {
        // there was a problem
        showError(msg.status + " " + msg.statusText);
      }
    });
  }
  
  this.addContact = function() { 
    // Create the UUID
    var newUUID = "BwABC-" + Math.uuid();
    
    // build the vcard
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    vcData += "UID:" + newUUID + "\n";
    vcData += "FN:" + $("#FIRSTNAME").val() + " " + $("#LASTNAME").val() + "\n";
    vcData += "N:" + $("#LASTNAME").val() + ";" + $("#FIRSTNAME").val() + ";;;\n";
    vcData += "KIND:individual\n";
    vcData += "ORG:" + $("#ORG").val() + ";;\n";
    vcData += "TITLE:" + $("#TITLE").val() + "\n";
    vcData += "NICKNAME:" + $("#NICKNAME").val() + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    $(".emailFields").each(function(index) {
      vcData += "EMAIL;TYPE=" + $("#EMAILTYPE-" + index).val() + ":" + $("#EMAIL-" + index).val() + "\n";
    });
    $(".phoneFields").each(function(index) {
      vcData += "TEL;TYPE=" + $("#PHONETYPE-" + index).val() + ":" + $("#PHONE-" + index).val() + "\n";
    });
    $(".addrFields").each(function(index) {
      vcData += "ADR;TYPE=" + $("#ADDRTYPE-" + index).val() + ":" + $("#POBOX-" + index).val() + ";" + $("#EXTADDR-" + index).val() + ";" + $("#STREET-" + index).val() + ";" + $("#CITY-" + index).val() + ";" +  $("#STATE-" + index).val() + ";" + $("#POSTAL-" + index).val() + ";" + $("#COUNTRY-" + index).val() + "\n";
    });
    //vcData += "GEO:TYPE=" + $("#ADDRTYPE-01").val() + ":geo:" + $("#GEO-01").val() + "\n";;
    vcData += "URL:" + $("#WEBPAGE").val() + "\n";
    vcData += "PHOTO;VALUE=uri:" + $("#PHOTOURL").val() + "\n";
    vcData += "NOTE:" + $("#NOTE").val() + "\n";
    vcData += "END:VCARD";
    
    this.addEntry(vcData,newUUID,"#contactForm");
  };
  
  this.updateContact = function() {
    var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    if (curCard.UID == undefined) {
      // if the card doesn't have a UID (probably because it was imported), give it one
      vcData+= "UID:BwABC-" + Math.uuid() + "\n";
    } else {
      vcData += "UID:" + curCard.UID[0].value + "\n";
    } 
    vcData += "FN:" + $("#FIRSTNAME").val() + " " + $("#LASTNAME").val() + "\n";
    vcData += "N:" + $("#LASTNAME").val() + ";" + $("#FIRSTNAME").val() + ";;;\n";
    vcData += "KIND:individual\n";
    vcData += "ORG:" + $("#ORG").val() + ";;\n";
    vcData += "TITLE:" + $("#TITLE").val() + "\n";
    vcData += "NICKNAME:" + $("#NICKNAME").val() + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    $(".emailFields").each(function(index) {
      vcData += "EMAIL;TYPE=" + $("#EMAILTYPE-" + index).val() + ":" + $("#EMAIL-" + index).val() + "\n";
    });
    $(".phoneFields").each(function(index) {
      vcData += "TEL;TYPE=" + $("#PHONETYPE-" + index).val() + ":" + $("#PHONE-" + index).val() + "\n";
    });
    $(".addrFields").each(function(index) {
      vcData += "ADR;TYPE=" + $("#ADDRTYPE-" + index).val() + ":" + $("#POBOX-" + index).val() + ";" + $("#EXTADDR-" + index).val() + ";" + $("#STREET-" + index).val() + ";" + $("#CITY-" + index).val() + ";" +  $("#STATE-" + index).val() + ";" + $("#POSTAL-" + index).val() + ";" + $("#COUNTRY-" + index).val() + "\n";
    });
    //vcData += "GEO:TYPE=" + $("#ADDRTYPE-" + index).val() + ":geo:" + $("#GEO-" + index).val() + "\n";;
    vcData += "URL:" + $("#WEBPAGE").val() + "\n";
    vcData += "PHOTO;VALUE=uri:" + $("#PHOTOURL").val() + "\n";
    vcData += "NOTE:" + $("#NOTE").val() + "\n";
    vcData += "END:VCARD";
    
    this.updateEntry(vcData,curCard.href,curCard.etag,"#contactForm");
  };

  // ********************
  // IMPORT FORM HANDLING
  // ********************
  
  this.importVcards = function() {
    var vcardData = $("#bwImportText").val();
    var curCards = new Array();
    var importMessages = "<ol>";
    curCards = separateIntoCards(vcardData);
    
    showMessage(bwAbDispImportProcessingTitle,bwAbDispImportProcessing,true); 
    
    for (var i=0; i < curCards.length; i++) {    
      // get the UUID
      var UUID = getUUID(curCards[i]);
      // is it new?
      var isNew = true;
      var etag = "";
      
      // no UID?  Make one.
      if (!UUID || UUID == undefined) {
        UUID = "BwABC-Imp-" + Math.uuid();
      } else {
        // see if we already have a card with this UUID
        for (var j=0; j < bwAddressBook.books.length; j++) {
          var book = bwAddressBook.books[j];
          for (var k=0; k < book.vcards.length; k++) {
            var existingCard = jQuery.parseJSON(book.vcards[k]);
            if (existingCard.UID != undefined && existingCard.UID[0].value != undefined) {
              if (existingCard.UID[0].value == UUID) {
                isNew = false;
                etag = existingCard.etag;
                break;
              }
            }
          }
          if (!isNew) {
            break;
          }
        }
      }
            
      var addrBookUrl = bwAddressBook.defPersBookUrl;

      $.ajax({
        type: "put",
        url: addrBookUrl + UUID + ".vcf",
        data: curCards[i],
        dataType: "text",
        async: false,
        processData: false,
        beforeSend: function(xhrobj) {
          xhrobj.setRequestHeader("X-HTTP-Method-Override", "PUT");
          if (isNew) {
            xhrobj.setRequestHeader("If-None-Match", "*");
          } else {
            xhrobj.setRequestHeader("If-Match", '"' + etag + '"');
          }
          xhrobj.setRequestHeader("Content-Type", "text/vcard");
        },
        success: function(responseData, status){
          importMessages += '<li>' + status + ' ' + responseData;
          if (isNew) {
            importMessages += bwAbDispImportNew;
          } else {
            importMessages += bwAbDispImportUpdate;
          }
          importMessages += '</li>';
        },
        error: function(msg) {
          // there was a problem
          importMessages += '<li><strong>' + msg.status + ' ' + msg.statusText + '</strong><br/>';
          importMessages += '<span class="pre">' + $ESAPI.encoder().encodeForHTML(String(curCards[i])) + '</span></li>';
        }
      });
    };

    // we can show all messages at the end because we are synchronous 
    importMessages += '</ol><div style="text-align: center; margin-top: 2em;"><button type="button" onclick="window.location.reload();">' + bwAbDispImportDoneButton + '</button>';
    showMessage(bwAbDispImportStatus,importMessages,true); 
    clearFields("#importForm");
    
  };
  
  // ********************
  // EXPORT HANDLING
  // ********************
  
  // Should only export the currently selected book.
  // For now, that's the single personal book
  this.exportVcards = function() {

    // perform a get on the book, setting the accept headers and content type
    $.ajax({
      type: "get",
      url: bwAddressBook.defPersBookUrl,
      dataType: "text",
      processData: false,
      async: false,
      //data: content,
      beforeSend: function(xhrobj) {
        xhrobj.setRequestHeader("Depth", "1");
        xhrobj.setRequestHeader("Content-Type", "text/vcard;charset=UTF-8");
        xhrobj.setRequestHeader("Accept", "text/vcard");
      },
      success: function(responseData) {
        showMessage(bwAbDispExportTitle,bwAbDispExported,true);
        /*var exportWindow = window.open();
        exportWindow.document.write(String(responseData));
        exportWindow.document.close();*/
        
      },
      error: function(msg) {
        // there was a problem
        var error = msg.statusText;
        if (msg.status == "404") {
          error = bwAbDispError404;
          bwAddressBook.userid = bwAbDispErrorAccessDenied;
        }
        if (msg.status == "500") {
          error = bwAbDispError500;
        }
        showError(error);
      }
    });
  };
  
  // ******************************
  // VIEW VCARD SERVER ADDRESS BOOK
  // ******************************

  this.viewBookUrl = function() {
    var viewBookWindow = window.open(bwAddressBook.defPersBookUrl,"cardDAVBook");
  };

  // *******************
  // GROUP FORM HANDLING
  // *******************

  this.addGroup = function() {
    // Create the UUID
    var newUUID = "BwABC-" + Math.uuid();
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    vcData += "UID:" + newUUID + "\n";
    vcData += "FN:" + $.trim($("#GROUP-NAME").val()) + "\n";
    vcData += "N:" + $.trim($("#GROUP-NAME").val()) + ";;;;\n";
    vcData += "KIND:group\n";
    vcData += "ORG:" + $.trim($("#GROUP-ORG").val()) + ";;\n";
    vcData += "NICKNAME:" + $.trim($("#GROUP-NICKNAME").val()) + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    vcData += "NOTE:" + $.trim($("#GROUP-NOTE").val()) + "\n";
    vcData += "END:VCARD";
    
    this.addEntry(vcData,newUUID,"#groupForm");
  };
  
  this.updateGroup = function() {
    var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    if (curCard.UID == undefined) {
      // if the card doesn't have a UID (probably because it was imported), give it one
      vcData+= "UID:BwABC-" + Math.uuid() + "\n";
    } else {
      vcData += "UID:" + curCard.UID[0].value + "\n";
    }
    vcData += "FN:" + $.trim($("#GROUP-NAME").val()) + "\n";
    vcData += "N:" + $.trim($("#GROUP-NAME").val()) + ";;;;\n";
    vcData += "KIND:group\n";
    vcData += "ORG:" + $.trim($("#GROUP-ORG").val()) + ";;\n";
    vcData += "NICKNAME:" + $.trim($("#GROUP-NICKNAME").val()) + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    vcData += "NOTE:" + $.trim($("#GROUP-NOTE").val()) + "\n";
    if (curCard.MEMBER != undefined) {
      for (var i=0; i<curCard.MEMBER.length; i++) {
        // no need for mailto: here - it's in the value
        vcData += "MEMBER:" + curCard.MEMBER[i].value + "\n";
      }; 
    };
    vcData += "END:VCARD";
    
    this.updateEntry(vcData,curCard.href,curCard.etag,"#groupForm");
  };
  
  // group is a json object
  // memberMailTo is a mailto address -- we won't arrive here without it
  this.addMemberToGroup = function(bookIndex,groupIndex,memberBookIndex,memberIndex) {
    // get the current member to be added
    var curMember = jQuery.parseJSON(bwAddressBook.books[memberBookIndex].vcards[memberIndex]);
    
    if (curMember.KIND[0].value == "group"){
      // can't add groups to groups
      showMessage(bwAbDispDisallowed,bwAbDispNoMemberAddGroup,true);
    } else if (curMember.EMAIL == undefined) {
      // the member has no email (mailto) address, so disallow adding it to a group
      showMessage(bwAbDispDisallowed,bwAbDispNoMemberAddEmail,true);
    } else {
      // get the group
      var curGroup = jQuery.parseJSON(bwAddressBook.books[bookIndex].vcards[groupIndex]);
      
      // check to see if the entry is already in the group and abort if so
      if (curGroup.MEMBER != undefined) {
        for(var i=0; i<curGroup.MEMBER.length; i++) {
          if(curGroup.MEMBER[i].value.stripTags() == "mailto:" + curMember.EMAIL[0].value.stripTags()) {
            showMessage(bwAbDispAlreadyAMemberTitle,bwAbDispAlreadyAMember,true);
            return;
          }
        }
      }
      
      // add the member to the group
      // check for the existence of the properties (UID must be ok or we should simply fail out)
      var fn ="";
      if(curGroup.FN != undefined) { 
        fn = curGroup.FN[0].value.stripTags(); 
      }
      var nickname ="";
      if(curGroup.NICKNAME != undefined) { 
        nickname = curGroup.NICKNAME[0].value.stripTags(); 
      }
      var org = "";
      if(curGroup.ORG != undefined) { 
        org = curGroup.ORG[0].values.organization_name.stripTags(); 
      }
      var note = "";
      if(curGroup.NOTE != undefined) { 
        url = curGroup.NOTE[0].value.stripTags(); 
      } 
      
      // now let's build the vcard
      var vcData = "BEGIN:VCARD\n"
      vcData += "VERSION:4.0\n";
      if (curGroup.UID == undefined) {
        // if the card doesn't have a UID (probably because it was imported), give it one
        vcData+= "UID:BwABC-" + Math.uuid() + "\n";
      } else {
        vcData += "UID:" + curGroup.UID[0].value + "\n";
      } 
      vcData += "FN:" + fn + "\n";
      vcData += "N:" + fn + ";;;;\n";
      vcData += "KIND:group\n";
      vcData += "ORG:" + org + ";;\n";
      vcData += "NICKNAME:" + nickname + "\n";
      vcData += "CLASS:PRIVATE\n";
      vcData += "REV:" + getRevDate() + "\n";
      vcData += "NOTE:" + note + "\n";
      if (curGroup.MEMBER != undefined) {
        for (var i=0; i<curGroup.MEMBER.length; i++) {
          // no need for mailto: here - it's in the value
          vcData += "MEMBER:" + curGroup.MEMBER[i].value + "\n";
        }; 
      };
      // now tag on the new member:
      vcData += "MEMBER:mailto:" + $.trim(curMember.EMAIL[0].value) + "\n";
      vcData += "END:VCARD";
      
      this.updateEntry(vcData,curGroup.href,curGroup.etag);
    };
  };
  
  //accepts either a card object or will pick out the current card from the address book
  this.updateGroupMembers = function(group) {
    var curGroup = "";
    if (group != undefined && group != null) {
      curGroup = group;
    } else {
      var curGroup = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    }
    
    // check for the existence of the properties (UID must be ok or we should simply fail out)
    var fn ="";
    if(curGroup.FN != undefined) { 
      fn = curGroup.FN[0].value.stripTags(); 
    }
    var nickname ="";
    if(curGroup.NICKNAME != undefined) { 
      nickname = curGroup.NICKNAME[0].value.stripTags(); 
    }
    var org = "";
    if(curGroup.ORG != undefined) { 
      org = curGroup.ORG[0].values.organization_name.stripTags(); 
    }
    var note = "";
    if(curGroup.NOTE != undefined) { 
      url = curGroup.NOTE[0].value.stripTags(); 
    } 
    
    // now let's build the vcard
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    if (curGroup.UID == undefined) {
      // if the card doesn't have a UID (probably because it was imported), give it one
      vcData+= "UID:BwABC-" + Math.uuid() + "\n";
    } else {
      vcData += "UID:" + curGroup.UID[0].value + "\n";
    } 
    vcData += "FN:" + fn + "\n";
    vcData += "N:" + fn + ";;;;\n";
    vcData += "KIND:group\n";
    vcData += "ORG:" + org + ";;\n";
    vcData += "NICKNAME:" + nickname + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    vcData += "NOTE:" + note + "\n";
    if (curGroup.MEMBER != undefined) {
      for (var i=0; i<curGroup.MEMBER.length; i++) {
        // no need for mailto: here - it's in the value
        vcData += "MEMBER:" + curGroup.MEMBER[i].value + "\n";
      }; 
    };
    vcData += "END:VCARD";
    
    this.updateEntry(vcData,curGroup.href,curGroup.etag);
  };
    
  // **********************
  // LOCATION FORM HANDLING
  // **********************

  this.addLocation = function() {
    // Create the UUID
    var newUUID = "BwABC-" + Math.uuid();
   
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    vcData += "UID:" + newUUID + "\n";
    vcData += "FN:" + $.trim($("#LOCATION-NAME").val()) + "\n";
    vcData += "N:" + $.trim($("#LOCATION-NAME").val()) + ";;;;\n";
    vcData += "KIND:location\n";
    vcData += "ORG:" + $.trim($("#LOCATION-ORG").val()) + ";;\n";
    vcData += "NICKNAME:" + $.trim($("#LOCATION-NICKNAME").val()) + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    vcData += "EMAIL:" + $.trim($("#LOCATION-EMAIL").val()) + "\n";
    vcData += "TEL:" + $.trim($("#LOCATION-PHONE").val()) + "\n";  
    vcData += "ADR:" + $.trim($("#LOCATION-POBOX").val()) + ";" + $.trim($("#LOCATION-EXTADDR").val()) + ";" + $.trim($("#LOCATION-STREET").val()) + ";" + $.trim($("#LOCATION-CITY").val()) + ";" +  $.trim($("#LOCATION-STATE").val()) + ";" + $.trim($("#LOCATION-POSTAL").val()) + ";" + $.trim($("#LOCATION-COUNTRY").val()) + "\n";
    //vcData += "GEO:TYPE=" + $.trim($("#ADDRTYPE-01").val()) + ":geo:" + $.trim($("#GEO-01").val()) + "\n";;
    vcData += "URL:" + $.trim($("#LOCATION-WEBPAGE").val()) + "\n";
    vcData += "PHOTO;VALUE=uri:" + $.trim($("#LOCATION-PHOTOURL").val()) + "\n";
    vcData += "NOTE:" + $.trim($("#LOCATION-NOTE").val()) + "\n";
    vcData += "END:VCARD";
    
    this.addEntry(vcData,newUUID,"#locationForm");
  };
  
  this.updateLocation = function() {
    var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    
    var vcData = "BEGIN:VCARD\n"
    vcData += "VERSION:4.0\n";
    if (curCard.UID == undefined) {
      // if the card doesn't have a UID (probably because it was imported), give it one
      vcData+= "UID:BwABC-" + Math.uuid() + "\n";
    } else {
      vcData += "UID:" + curCard.UID[0].value + "\n";
    } 
    vcData += "FN:" + $.trim($("#LOCATION-NAME").val()) + "\n";
    vcData += "N:" + $.trim($("#LOCATION-NAME").val()) + ";;;;\n";
    vcData += "KIND:location\n";
    vcData += "ORG:" + $.trim($("#LOCATION-ORG").val()) + ";;\n";
    vcData += "NICKNAME:" + $.trim($("#LOCATION-NICKNAME").val()) + "\n";
    vcData += "CLASS:PRIVATE\n";
    vcData += "REV:" + getRevDate() + "\n";
    vcData += "EMAIL:" + $.trim($("#LOCATION-EMAIL").val()) + "\n";
    vcData += "TEL:" + $.trim($("#LOCATION-PHONE").val()) + "\n";  
    vcData += "ADR:" + $.trim($("#LOCATION-POBOX").val()) + ";" + $.trim($("#LOCATION-EXTADDR").val()) + ";" + $.trim($("#LOCATION-STREET").val()) + ";" + $.trim($("#LOCATION-CITY").val()) + ";" +  $.trim($("#LOCATION-STATE").val()) + ";" + $.trim($("#LOCATION-POSTAL").val()) + ";" + $.trim($("#LOCATION-COUNTRY").val()) + "\n";
    //vcData += "GEO:TYPE=" + $.trim($("#ADDRTYPE-01").val()) + ":geo:" + $.trim($("#GEO-01").val()) + "\n";;
    vcData += "URL:" + $.trim($("#LOCATION-WEBPAGE").val()) + "\n";
    vcData += "PHOTO;VALUE=uri:" + $.trim($("#LOCATION-PHOTOURL").val()) + "\n";
    vcData += "NOTE:" + $.trim($("#LOCATION-NOTE").val()) + "\n";
    vcData += "END:VCARD";
    
    this.updateEntry(vcData,curCard.href,curCard.etag,"#locationForm");
  };  
  
  
  // *********************
  // DELETE AN ITEM
  // *********************
  
  // Note: deleteEntry works for contacts, locations, groups, and resources
  this.deleteEntry = function() {
    // For now, we'll assume there is only one book from which we can delete cards.
    // If we try to delete from another at the moment, we'll either have no access or get  
    // a 404 for not having the uuid in the book
    if(confirm(bwAbDispDeleteConfirm)) {
      // probably want to replace this confirm with a better dialog, but will certainly do for now.
      var addrBookUrl = bwAddressBook.defPersBookUrl;
      var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
      
      $.ajax({
        type: "delete",
        url: curCard.href,
        dataType: "xml",
        beforeSend: function(xhrobj) {
          xhrobj.setRequestHeader("X-HTTP-Method-Override", "DELETE");
        },
        success: function(responseData, status){
          // A SUCCESS IS A "204 No Content" which is trapped in the error block below
        },
        error: function(msg) {
          // if the message is a 204 No Content, we've actually got the correct 
          // response from the server so...treat it like a success:
          if (msg.status == "204") {
            // This is our success.
            // To make this compatible with the table sorter, just round-trip the data
            window.location.reload();
          } else {
          // there was a problem
            showError(msg.status + " " + msg.statusText);
          }
        }
      });
    }
  }
  
  // *******************
  // DISPLAY HANDLING
  // *******************
 
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
    var fullName = "";
    
    // Get the current kind.
    // If no kind, attempt to use "individual".
    var curKind = "individual";
    if (curCard.KIND != undefined) {
      curKind = curCard.KIND[0].value.stripTags();
    } 
    
    // build the details    
    // full name
    if(curCard.FN != undefined) { 
      fullName = curCard.FN[0].value.stripTags(); 
    }
    
    details += '<h1 class="' + curKind + '">' + fullName + '</h1>';
    
    // picture
    if(curCard.PHOTO != undefined) { 
      details += '<div class="detailPhoto"><img src="' + curCard.PHOTO[0].value.stripTags() + '" alt="' + bwAbDispDetailsPhoto + fullName + '"/></div>'; 
    }
    
    details += '<table id="bwDetailsTable">';
    // title
    if (curCard.TITLE != undefined && curCard.TITLE[0].value != "") {
      details += '<tr><td class="field">' + bwAbDispDetailsTitle + '</td><td>' + curCard.TITLE[0].value.stripTags() + '</td></tr>';
    }
    // organization
    if(curCard.ORG != undefined && curCard.ORG[0].values.organization_name != "") {
      details += '<tr><td class="field">' + bwAbDispDetailsOrg + '</td><td>' + curCard.ORG[0].values.organization_name.stripTags() + '</td></tr>';
    }
    // nickname
    if(curCard.NICKNAME != undefined && curCard.NICKNAME[0].value != "") {
      details += '<tr><td class="field">' + bwAbDispDetailsNickname + '</td><td>' + curCard.NICKNAME[0].value.stripTags() + '</td></tr>';
    }
    // email address(es)
    if(curCard.EMAIL != undefined) { 
      for (var i=0; i < curCard.EMAIL.length; i++) {
        details += '<tr><td class="field">' + bwAbDispDetailsEmail + '</td><td><a href="mailto:' + $ESAPI.encoder().encodeForHTML(curCard.EMAIL[i].value) + '">' + $ESAPI.encoder().encodeForHTML(curCard.EMAIL[i].value) + '</a></td></tr>';
      }
    }
    // telephone number(s)
    if (curCard.TEL != undefined) {
      for (var i=0; i < curCard.TEL.length; i++) {
        details += '<tr><td class="field">' + bwAbDispDetailsPhone.stripTags() + '</td>';
        details += '<td>' + curCard.TEL[i].values.number.stripTags();
        details += '</td></tr>';
      }
    }
    // url
    if (curCard.URL != undefined && curCard.URL[0].value != "") {
      details += '<tr><td class="field">' + bwAbDispDetailsUrl + '</td><td><a href="' + curCard.URL[0].value.stripTags() + '">' + curCard.URL[0].value.stripTags() + '</a></td></tr>';
    }
    // address(es)
    if(curCard.ADR != undefined) { 
      for (var i=0; i < curCard.ADR.length; i++) {
        details += '<tr class="newGrouping"><td class="field">' + bwAbDispDetailsAddress + '</td><td>';
        // output the address details:
        if (curCard.ADR[i].values.po_box != undefined && curCard.ADR[i].values.po_box != "") {
          details += curCard.ADR[i].values.po_box.stripTags() + "<br/>";
        }
        if (curCard.ADR[i].values.extended_address != undefined && curCard.ADR[i].values.extended_address != "") {
          details += curCard.ADR[i].values.extended_address.stripTags() + "<br/>";
        }
        if (curCard.ADR[i].values.street_address != undefined && curCard.ADR[i].values.street_address != "") {
          details += curCard.ADR[i].values.street_address.stripTags() + "<br/>";
        }
        if (curCard.ADR[i].values.locality != undefined && curCard.ADR[i].values.locality != "") {
          details += curCard.ADR[i].values.locality.stripTags();
        }
        if (curCard.ADR[i].values.state != undefined && curCard.ADR[i].values.state != "") {
          details += ", " + curCard.ADR[i].values.state.stripTags() + " ";
        }
        if (curCard.ADR[i].values.postal_code != undefined && curCard.ADR[i].values.postal_code != "") {
          details += curCard.ADR[i].values.postal_code.stripTags() + "<br/>";
        }
        if (curCard.ADR[i].values.country != undefined && curCard.ADR[i].values.country != "") {
          details += curCard.ADR[i].values.country.stripTags();
        }
        details += '</td></tr>';
      }
    }
    // group members, if a group 
    if (curKind == "group") {
      if (curCard.MEMBER != undefined) {
        details += '<tr class="newGrouping"><td class="field">' + bwAbDispDetailsGroupMembers + '</td><td>';
        details += '<table id="groupMembers">';
        for (var i=0; i < curCard.MEMBER.length; i++) {
          details += '<tr><td>' + curCard.MEMBER[i].value.substring(curCard.MEMBER[i].value.indexOf(":")+1).stripTags() + '</td>';
          details += '<td><a href="#" class="bwRemoveMember">' + bwAbDispDetailsRemoveMember + '</a></td></tr>';
        }
        details += '<tr id="memberRemovalRow"><td></td><td>';
        details += '<button id="commitMemberRemoval">' + bwAbDispDetailsCommitMemberRemoval + '</button> ';
        details += '<button id="cancelMemberRemoval">' + bwAbDispDetailsCancel + '</button></td></tr>';
        details += '</table></td></tr>';
      }
    }
    // note
    if(curCard.NOTE != undefined && curCard.NOTE[0].value != "") {
      details += '<tr class="newGrouping"><td class="field">' + bwAbDispDetailsNote + '</td><td>' + curCard.NOTE[0].value.stripTags() + '</td></tr>';
    }
    details += '</table>';
            
    // write out the output
    $("#bwAddrBookOutputDetails").html(details);
    
    // if a group, now bind onclick events to the elements that need it 
    // (remove member, commit removal, cancel)
    if (curKind == "group") {
      // remove a member
      $(".bwRemoveMember").click(function() {
        // get the position of the current table row - it is the same as the object in the json object
        var position = $(this).parents("tr").index()-1;
        // remove the member
        curCard.MEMBER.splice(position,1);
        // hide the current row
        $(this).parent().parent().fadeTo(350, 0, function () { 
          $(this).remove();
        });
        // display the commit buttons
        $("#memberRemovalRow").show();
      });
      
      // commit member removals to server
      $("#commitMemberRemoval").click(function() {
        bwAddrBook.updateGroupMembers(curCard);    
      });

      // cancel member removals
      $("#cancelMemberRemoval").click(function() {
        // for now, just round trip to restore original state
        window.location.reload();    
      });
    }
    
    showPage("bw-details");
  }
  
  // *******************
  // FILTERING
  // *******************

  
    
  // *******************
  // GETTERS AND SETTERS
  // *******************

  this.setBook = function(val) {
    bwAddressBook.book = val; 
  }
  
  this.setCard = function(val) {
    bwAddressBook.card = val; 
  }
  
};

// Now build it
$(document).ready(function() {

  bwAddrBook = new bwAddressBook();
  
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
  
  // assign the userid after stripping and encoding
  userid = $ESAPI.encoder().encodeForHTML(qsParameters.user.stripTags());
  
  
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
  
  // make our list tables sortable
  $("#bwAddrBookOutputList table").tablesorter({sortList: [[0,0], [1,0]],widgets: ['zebra']});
   
  
  /****************************
   * EVENT HANDLERS:
   ****************************/
  
  // *****************
  // DISPLAY HANDLERS
  // *****************
  
  // select a book or subscription to display
  $(".bwBookLink").click(function() {
    // extract the book array index from the id
    bwAddrBook.setBook($(this).attr("id").substr($(this).attr("id").indexOf("-")+1));
    
    // remove highlighting from all menu items
    $("#booksAndGroups a").each(function(index){
      $(this).removeClass("selected");
    });
    // now highlight the one just selected
    $(this).addClass("selected");
    
    bwAddrBook.display();
    
    // set a cookie so we can hold on to the current menu item
    // between page refreshes
    //$.cookie("selectedMenuId",$(this).attr("id"));
    //alert($.cookie("selectedMenuId"));
  });
  
  // display group details
  $(".bwGroup").click(function() {
    // get the part of the id that holds the indices
    var indices = $(this).attr("id").substr($(this).attr("id").indexOf("-")+1);
    // extract the book array index from the id
    bwAddrBook.setBook(indices.substr(0,indices.indexOf("-")));
    // extract the item index from the id
    bwAddrBook.setCard(indices.substr(indices.indexOf("-")+1));
    
    // remove highlighting from all menu items
    $("#booksAndGroups a").each(function(index){
      $(this).removeClass("selected");
    });
    // now highlight the one just selected
    $(this).addClass("selected");
    
    bwAddrBook.showDetails();
    
    // set a cookie so we can hold on to the current menu item
    // between page refreshes
    //$.cookie("selectedMenuId",$(this).attr("id"));
    //alert($.cookie("selectedMenuId"));
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
  
  // button to return to list from detail view
  $("#backToList").click(function() {
    showPage("bw-list");
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

  
  // *****************
  //  FORM HANDLING
  // *****************
  
  // ADDING
  
  // show form for adding a new contact
  $("#addContact").click(function() {
    clearFields("#contactForm");
    $("#bw-modContact h3").text(bwAbDispAddContact);
    $("#submitContact").attr("class","add");
    $("#contactForm").attr("action","#add");
    $("#submitContact").text(bwAbDispAddContact);
    
    // make sure any appended fields are removed
    $("#contactForm .emailFields").each(function(index){
      if (index > 0) {
        $(this).remove(); 
      }
    });
    $("#contactForm .phoneFields").each(function(index){
      if (index > 0) {
        $(this).remove(); 
      }
    });
    
    showPage("bw-modContact");
    $("#FIRSTNAME").focus();
  });
  
  // show form for adding a group
  $("#addGroup").click(function() {
    clearFields("#groupForm");
    $("#bw-modGroup h3").text(bwAbDispAddGroup);
    $("#submitGroup").attr("class","add");
    $("#groupForm").attr("action","#add");
    $("#submitGroup").text(bwAbDispAddGroup);
    showPage("bw-modGroup");
    $("#GROUP-NAME").focus();
  });
  
  // show form for adding a location
  $("#addLocation").click(function() {
    clearFields("#locationForm");
    $("#bw-modLocation h3").text(bwAbDispAddLocation);
    $("#submitLocation").attr("class","add");
    $("#locationForm").attr("action","#add");
    $("#submitLocation").text(bwAbDispAddLocation);
    showPage("bw-modLocation");
    $("#LOCATION-NAME").focus();
  });

  // show form for adding/editing a resource (not yet available)
  $("#addResource").click(function() {
    showPage("bw-modResource");
  });
  
  // show form for import
  $("#importContacts").click(function() {
    showPage("bw-import");
  });
  
  // do a global export
  $("#exportContacts").click(function() {
    bwAddrBook.exportVcards();
  });
  
  // view the address book from the vCard server
  $("#viewBookUrl").click(function() {
    bwAddrBook.viewBookUrl();
  });
  
  /* disable book droppables for now - while we have only one assumed 
   * droppable book, additions will be made directly from 
   * search results     
   $("#booksAndGroups a.bwBook").droppable({ 
    accept: '#bwAddrBookOutputList td.name',
    greedy: true,
    hoverClass: 'droppableHighlight',  
    drop:   function () { 
      showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
      return false; 
    } 
  });
  */ 
  
  // add a member to group by dragging and dropping
  $("#booksAndGroups a.bwGroup").droppable({ 
    accept: '#bwAddrBookOutputList td.name', 
    greedy: true,
    hoverClass: 'droppableHighlight',
    drop:   function (event, ui) { 
      // get the group indices
      var groupRef = $(this).attr("id").substr($(this).attr("id").indexOf("-")+1);
      var bookIndex = groupRef.substring(0,groupRef.indexOf("-"));
      var groupIndex = groupRef.substr(groupRef.indexOf("-")+1);
      
      // get the member indices
      var memberRef = ui.draggable.attr("id").substr(ui.draggable.attr("id").indexOf("-")+1);
      var memberBookIndex = memberRef.substring(0,memberRef.indexOf("-"));
      var memberIndex = memberRef.substr(memberRef.indexOf("-")+1);
      
      // pass them to the update method
      bwAddrBook.addMemberToGroup(bookIndex,groupIndex,memberBookIndex,memberIndex);
      return false; 
    } 
  }); 
  
  
  // EDITING
  
  //show form for editing an item
  $("#editEntry").click(function() {
    // get the current vcard
    var curCard = jQuery.parseJSON(bwAddressBook.books[bwAddressBook.book].vcards[bwAddressBook.card]);
    // get its KIND, defaulting to "individual"
    var curKind = "individual";
    if (curCard.KIND != undefined) {
      curKind = curCard.KIND[0].value;
    }
    
    // Setup the form fields.  This is dependent on 
    // calling this function from the details view
    // where we have a current card loaded.
    setupFormFields(curCard,curKind);
    
    // branch on the type of entry, 
    // fix up buttons and titles, and show the page: 
    switch(curKind) {
      case "location": 
        $("#bw-modLocation h3").text(bwAbDispUpdateLocation);
        $("#submitLocation").attr("class","update");
        $("#locationForm").attr("action","#update");
        $("#submitLocation").text(bwAbDispUpdateLocation);
        // now show the page
        showPage("bw-modLocation");
        $("#LOCATION-NAME").focus();
        break;
      case "group":
        $("#bw-modGroup h3").text(bwAbDispUpdateGroup);
        $("#submitGroup").attr("class","update");
        $("#groupForm").attr("action","#update");
        $("#submitGroup").text(bwAbDispUpdateGroup);
        // now show the page
        showPage("bw-modGroup");
        $("#GROUP-NAME").focus();
        break;
      case "thing":
        break;
      default:  // this is the "individual" kind
        $("#bw-modContact h3").text(bwAbDispUpdateContact);
        $("#submitContact").attr("class","update");
        $("#contactForm").attr("action","#update");
        $("#submitContact").text(bwAbDispUpdateContact);
        // now show the page
        showPage("bw-modContact");
        $("#FIRSTNAME").focus();
    }
      
  });
  
  // SUBMITTING AND CANCELLING
  
  // submit a contact to the server
  $("#submitContact").click(function() {
    if ($("#submitContact").hasClass('update')) {
      bwAddrBook.updateContact();
    } else {
      bwAddrBook.addContact();
    }
  });
  
  $("#cancelContact").click(function() {
    clearFields("#contactForm");
    if ($("#submitContact").hasClass('update')) {
      showPage("bw-details");
    } else {
      showPage("bw-list");
    }
  });
  
  //submit a group to the server
  $("#submitGroup").click(function() {
    if ($("#submitGroup").hasClass('update')) {
      bwAddrBook.updateGroup();
    } else {
      bwAddrBook.addGroup();
    }
  });
  
  $("#cancelGroup").click(function() {
    clearFields("#groupForm");
    if ($("#submitGroup").hasClass('update')) {
      showPage("bw-details");
    } else {
      showPage("bw-list");
    }
  });
  
  //submit a location to the server
  $("#submitLocation").click(function() {
    if ($("#submitLocation").hasClass('update')) {
      bwAddrBook.updateLocation();
    } else {
      bwAddrBook.addLocation();
    }
  });
  
  $("#cancelLocation").click(function() {
    clearFields("#locationForm");
    if ($("#submitLocation").hasClass('update')) {
      showPage("bw-details");
    } else {
      showPage("bw-list");
    }
  });
  
  
  // import vcards
  $("#submitImport").click(function() {
    bwAddrBook.importVcards();
  });
  
  $("#cancelImport").click(function() {
    clearFields("#importForm");
    showPage("bw-list");
  });
  
  
  // DELETING
  
  // delete a vcard (individual, group, location, or thing) from the address book
  $("#deleteEntry").click(function() {
    bwAddrBook.deleteEntry();
  });
  
  
  // SEARCHING AND FILTERING
  
  // letter filters 
  $("#filterLetters a").click(function() {
    scrollToItem(self.location.hash); 
  });
  
  // search and filter box
  $("#searchButton").click(function() {
    showMessage(bwAbDispUnimplementedTitle,bwAbDispUnimplemented,true);
    return false;
  });
  
  
  // FORM APPENDERS
  
  // add a set of address fields to the contact form
  $("#bwAppendAddr").click(function() {
    // get the id of the last email group
    var lastId = $(".addrFields:last").attr("id");
    // extract the number from the id and increment it
    var i = Number(lastId.substring(lastId.indexOf("-")+1))+1;
    // build the new fields with the new index (i)
    var newAddrFields = buildAddrFields(i);
    // append the result to the dom
    $("#bwContactAddrHolder").append(newAddrFields);
  });
  
  // add a new email address field to the contact form
  $("#bwAppendEmail").click(function() {
    var lastId = $(".emailFields:last").attr("id");
    var i = Number(lastId.substring(lastId.indexOf("-")+1))+1;
    var newEmailFields = buildEmailFields(i);
    $("#bwContactEmailHolder").append(newEmailFields);
  });
  
  // add a new phone field to the contact form
  $("#bwAppendPhone").click(function() {
    var lastId = $(".phoneFields:last").attr("id");
    var i = Number(lastId.substring(lastId.indexOf("-")+1))+1;
    var newPhoneFields = buildPhoneFields(i);
    $("#bwContactPhoneHolder").append(newPhoneFields);
  });
  
});

/****************************
 * HELPER FUNCTIONS:
 ****************************/
function setupFormFields(curCard,kind) {
  // branch on the KIND of vcard to fill the correct form
  switch(kind) {
    case "location" :
      if (curCard.FN != undefined) $("#LOCATION-NAME").val(curCard.FN[0].value.stripTags());
      if (curCard.ORG != undefined) $("#LOCATION-ORG").val(curCard.ORG[0].values.organization_name.stripTags());
      if (curCard.NICKNAME != undefined) $("#LOCATION-NICKNAME").val(curCard.NICKNAME[0].value.stripTags());
      if (curCard.EMAIL != undefined) $("#LOCATION-EMAIL").val(curCard.EMAIL[0].value.stripTags());
      if (curCard.TEL != undefined) $("#LOCATION-PHONE").val(curCard.TEL[0].values.number.stripTags());
      if (curCard.ADR != undefined) {
        $("#LOCATION-POBOX").val(curCard.ADR[0].values.po_box.stripTags());
        $("#LOCATION-EXTADDR").val(curCard.ADR[0].values.extended_address.stripTags());
        $("#LOCATION-STREET").val(curCard.ADR[0].values.street_address.stripTags());
        $("#LOCATION-CITY").val(curCard.ADR[0].values.locality.stripTags());
        $("#LOCATION-STATE").val(curCard.ADR[0].values.state.stripTags());
        $("#LOCATION-POSTAL").val(curCard.ADR[0].values.postal_code.stripTags());
        $("#LOCATION-COUNTRY").val(curCard.ADR[0].values.country.stripTags());
        // $("#GEO-01").val(curCard.GEO[0].value); -- set when we have geo working
      }
      if (curCard.URL != undefined) $("#LOCATION-WEBPAGE").val(curCard.URL[0].value.stripTags());
      if (curCard.PHOTO != undefined) $("#LOCATION-PHOTOURL").val(curCard.PHOTO[0].value.stripTags());
      if (curCard.NOTE != undefined) $("#LOCATION-NOTE").val(curCard.NOTE[0].value.stripTags());
      break;
    case "group" :
      if (curCard.FN != undefined) $("#GROUP-NAME").val(curCard.FN[0].value.stripTags());
      if (curCard.ORG != undefined) $("#GROUP-ORG").val(curCard.ORG[0].values.organization_name.stripTags());
      if (curCard.NICKNAME != undefined) $("#GROUP-NICKNAME").val(curCard.NICKNAME[0].value.stripTags());
      break;
    default: // this is the "individual" KIND
      if (curCard.N != undefined) {
        $("#FIRSTNAME").val(curCard.N[0].values.given_names.stripTags());
        $("#LASTNAME").val(curCard.N[0].values.family_name.stripTags());
      }
      if (curCard.ORG != undefined) $("#ORG").val(curCard.ORG[0].values.organization_name.stripTags());
      if (curCard.TITLE != undefined) $("#TITLE").val(curCard.TITLE[0].value.stripTags());
      if (curCard.NICKNAME != undefined) $("#NICKNAME").val(curCard.NICKNAME[0].value.stripTags());
      if (curCard.TITLE != undefined) $("#TITLE").val(curCard.TITLE[0].value.stripTags());
      if (curCard.EMAIL != undefined) {
        for (var i=0; i < curCard.EMAIL.length; i++) {
          if (i > 0) {
            var newEmailFields = buildEmailFields(i);
            $("#bwContactEmailHolder").append(newEmailFields);
          }
          if (curCard.EMAIL[i].params['parameter-value'] != undefined) {
            $("#EMAILTYPE-" + i).val(curCard.EMAIL[i].params['parameter-value'].stripTags()); // this won't do
          }
          $("#EMAIL-" + i).val(curCard.EMAIL[i].value);
        }
        
        // make sure any extra fields appended from a previous edit are removed
        $("#contactForm .emailFields").each(function(index){
          if (index > i-1) {
            $(this).remove(); 
          }
        });
      }
      if (curCard.TEL != undefined) {
        for (var i=0; i < curCard.TEL.length; i++) {
          if (i > 0) {
            var newPhoneFields = buildPhoneFields(i); 
            $("#bwContactPhoneHolder").append(newPhoneFields);
          }
          if (curCard.TEL[i].params['parameter-value'] != undefined) {
            $("#PHONETYPE-" + i).val(curCard.TEL[i].params['parameter-value'].stripTags()); // this won't do
          }
          $("#PHONE-" + i).val(curCard.TEL[i].values.number.stripTags());
        }

        // make sure any extra fields appended from a previous edit are removed
        $("#contactForm .phoneFields").each(function(index){
          if (index > i-1) {
            $(this).remove(); 
          }
        });
      }
      if (curCard.ADR != undefined) {
        for (var i=0; i < curCard.ADR.length; i++) {
          if (i > 0) {
            var newAddrFields = buildAddrFields(i);
            $("#bwContactAddrHolder").append(newAddrFields);
          }
          if (curCard.ADR[0].params['parameter-value'] != undefined) {
            $("#ADDRTYPE-" + i).val(curCard.ADR[i].params['parameter-value'].stripTags()); // also won't do
          }
          $("#POBOX-" + i).val(curCard.ADR[i].values.po_box.stripTags());
          $("#EXTADDR-" + i).val(curCard.ADR[i].values.extended_address.stripTags());
          $("#STREET-" + i).val(curCard.ADR[i].values.street_address.stripTags());
          $("#CITY-" + i).val(curCard.ADR[i].values.locality.stripTags());
          $("#STATE-" + i).val(curCard.ADR[i].values.state.stripTags());
          $("#POSTAL-" + i).val(curCard.ADR[i].values.postal_code.stripTags());
          $("#COUNTRY-" + i).val(curCard.ADR[i].values.country.stripTags());
          //$("#GEO-" + i).val(curCard.GEO[i].value); -- set when we have geo working
        }
        
        // make sure any extra fields appended from a previous edit are removed
        $("#contactForm .addrFields").each(function(index){
          if (index > i-1) {
            $(this).remove(); 
          }
        });
      }
      if (curCard.URL != undefined) $("#WEBPAGE").val(curCard.URL[0].value.stripTags());
      if (curCard.PHOTO != undefined) $("#PHOTOURL").val(curCard.PHOTO[0].value.stripTags());
      if (curCard.NOTE != undefined) $("#NOTE").val(curCard.NOTE[0].value.stripTags());
  };
};

function buildEmailFields(i) {
  var emailFields = '<div class="emailFields" id="emailFields-' + i + '"><label class="bwField"  for="EMAIL-' + i + '">';
  emailFields += 'Email:</label><div class="bwValue"><select id="EMAILTYPE-' + i + '">';
  emailFields += '<option value="work">' + bwAbDispFormWork + '</option><option value="home">' + bwAbDispFormHome + '</option>';
  emailFields += '</select> <input type="text" size="40" value="" id="EMAIL-' + i + '"/></div>';
  emailFields += '<a class="bwRemove" onclick="bwRemoveItem(\'#emailFields-' + i + '\');"></a></div>';
  return emailFields;
};

function buildPhoneFields(i) {
  var phoneFields = '<div class="phoneFields" id="phoneFields-' + i + '">';
  phoneFields += '<label class="bwField" for="PHONE-' + i + '">Phone:</label>';
  phoneFields += '<div class="bwValue">';
  phoneFields += '  <select id="PHONETYPE-' + i + '">';
  phoneFields += '    <option value="work">' + bwAbDispFormWork + '</option>';
  phoneFields += '    <option value="home">' + bwAbDispFormHome + '</option>';
  phoneFields += '  </select>';
  phoneFields += '  <select id="TELTYPE-' + i + '">';
  phoneFields += '    <option value="voice">' + bwAbDispFormVoice + '</option>';
  phoneFields += '    <option value="cell">' + bwAbDispFormMobile + '</option>';
  phoneFields += '    <option value="fax">' + bwAbDispFormFax + '</option>';
  phoneFields += '    <option value="text">' + bwAbDispFormText + '</option>';
  phoneFields += '    <option value="pager">' + bwAbDispFormPager + '</option>';
  phoneFields += '  </select>';
  phoneFields += '  <input type="text" size="30" value="" id="PHONE-' + i + '"/>';
  phoneFields += '</div>';
  phoneFields += '<div class="bwRemove" onclick="bwRemoveItem(\'#phoneFields-' + i + '\');"></div>';
  phoneFields += '</div>';
  return phoneFields;
};

function buildAddrFields(i) {
  var addrFields = '<div class="addrFields" id="addrFields-' + i + '">';
  addrFields += '  <label class="bwField"  for="ADDRTYPE-' + i + '">Type:</label>';
  addrFields += '  <div class="bwValue">';
  addrFields += '    <select id="ADDRTYPE-' + i + '">';
  addrFields += '     <option value="work">work</option>';
  addrFields += '     <option value="home">home</option>';
  addrFields += '    </select>';
  addrFields += '    <label class="bwInternalField"  for="POBOX-' + i + '">P.O. Box:</label>';
  addrFields += '    <input type="text" size="6" value="" id="POBOX-' + i + '"/>';
  addrFields += '    <label class="bwInternalField"  for="EXTADDR-' + i + '">Apt/Suite:</label>';
  addrFields += '    <input type="text" size="12" value="" id="EXTADDR-' + i + '"/>';
  addrFields += '  </div>';
  addrFields += '  <div class="bwRemove" onclick="bwRemoveItem(\'#addrFields-' + i + '\');"></div>';
   
  addrFields += '  <label class="bwField"  for="STREET-' + i + '">Street:</label>';
  addrFields += '  <div class="bwValue"><input type="text" size="60" value="" id="STREET-' + i + '"/></div>';
  
  addrFields += '<label class="bwField"  for="CITY-' + i + '">City:</label>';
  addrFields += '  <div class="bwValue"><input type="text" size="60" value="" id="CITY-' + i + '"/></div>';
                  
  addrFields += '  <label class="bwField"  for="STATE-' + i + '">State/Province:</label>';
  addrFields += '  <div class="bwValue">';
  addrFields += '    <input type="text" size="20" value="" id="STATE-' + i + '"/>';
  addrFields += '    <label class="bwInternalField" for="POSTAL-' + i + '">Postal Code:</label>';
  addrFields += '    <input type="text" size="20" value="" id="POSTAL-' + i + '"/>';
  addrFields += '  </div>';
  
  addrFields += '  <label class="bwField"  for="COUNTRY-' + i + '">Country:</label>';
  addrFields += '  <div class="bwValue"><input type="text" size="60" value="" id="COUNTRY-' + i + '"/></div>';
  
  addrFields += '  <label class="bwField"  for="GEO-' + i + '">GEO:</label>';
  addrFields += '  <div class="bwValue"><input type="text" size="60" value="" id="GEO-' + i + '" disabled="disabled"/></div>';

  addrFields += '  <br class="clear"/>';
  addrFields += '</div>';
  return addrFields;
}

// remove an appended item
function bwRemoveItem(itemId) {
  $(itemId).remove();
};

// return the revision date
function getRevDate() {
  //build the revision date
  var now = new Date();
  var revDate = String(now.getUTCFullYear());
  revDate += String(now.getUTCMonthFull());
  revDate += String(now.getUTCDateFull()) + "T";
  revDate += String(now.getUTCHoursFull());
  revDate += String(now.getUTCMinutesFull());
  revDate += String(now.getUTCSecondsFull()) + "Z";
   
  return revDate;
};

//return the UUID of a vcard
function getUUID(vcard) {
  var lines = vcard.split('\n');
  for (var i=0;i<lines.length;i++) {
   var line = $.trim(lines[i]);
   if(line.indexOf("UID:")!= -1) {
     return line.substring(line.indexOf(":")+1);
   }
  }   
  return false;
};

/****************************
 * GENERIC FUNCTIONS:
 ****************************/
// display the named page
function showPage(pageId) {
  // first make all pages invisible
  $("#bw-pages > li").each(function(index){
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
};

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
