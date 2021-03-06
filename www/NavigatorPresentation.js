var execRaw = require('cordova/exec'),
  cordova = require('cordova');

// create and absolute url out of an relative one
var makeAbs = function(url) {
  var absUrl = null;
  try {
    absUrl = new URL(url, location.href).href;
  } catch (error) {
    return error;
  }
  if (!absUrl) {
    var a = document.createElement('a');
    a.href = url;
    absUrl = a.href;
  }
  if (!absUrl) {
    absUrl = url;
  }
  return absUrl;
};

// wrapper function to make calls to the java context
var exec = function() {
  var args = arguments;
  setTimeout(function() {
    execRaw.apply(undefined, args);
  }, 0);
};

// the presentation interface available in the controlling context
function NavigatorPresentation() {
  var defaultRequest;

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  Object.defineProperty(this, 'defaultRequest', {
    get: function() {
      return defaultRequest;
    },
    set: function(value) {
      if (typeof value === 'object' || value === null) {
        defaultRequest = value;
      }
    }
  });
}

module.exports = new NavigatorPresentation();