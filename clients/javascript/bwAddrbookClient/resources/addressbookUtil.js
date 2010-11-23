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

/** Bedework Address Book javascript client utility functions
 *
 * @author Arlen Johnson       johnsa - rpi.edu
 */


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
}

function scrollToItem(target) {
  if (target == undefined || target == '') {
    return true;
  } else {
    var hash = this.target;
    if (!target || !target.length) {
      return true;
    }
  }

  var $pane = $('.ui-layout-center');
  var $target = $(target);
  $target = $target.length && $target || $('[name='+ target.slice(1) +']');
  if ($target.length) {
    var targetTop = $target.offset().top;
    var paneTop = $pane.offset().top;
    $pane.animate({ scrollTop: '+='+ (targetTop - paneTop) +'px' }, 1000, 'linear', function(){
      self.location.replace(target); // make sure we scroll ALL the way!
    });
    return false; // cancel normal hyperlink
  }
}; 

/****************************
 * MESSAGE AND ERROR BOXES:
 ****************************/

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


/****************************
 * UTC FORMATTERS:
 ****************************/

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


/****************************
 * OUTPUT ENCODING:
 ****************************/

//Initialize ESAPI
org.owasp.esapi.ESAPI.initialize();

// strip all html tags from a string
// see also the ESAPI library included with this project
String.prototype.stripTags = function () {
  return this.replace(/<([^>]+)>/g,'');
}