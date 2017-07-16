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

function PresentationRequest(url) {
  var requestedUrl;
  var connection;
  var buildConnectionPromise;
  var presentationAvailability;
  var onAvailableChange;

  if (!url || url === '') {
    throw new Error('URL must not be empty');
  }

  if (typeof url === 'object' && url.length > 0) {
    url = url[0];
  }

  try {
    requestedUrl = makeAbs(url);
  } catch (e) {
    throw new SyntaxError('URL is malformed');
  }

  connection = new PresentationConnection(requestedUrl);
  buildConnectionPromise = new Promise(function(resolve, reject) {
    exec(function(result) {
      connection.id = result.id;
      resolve(connection);
    }, function(error) {
      reject();
      throw new Error('Failed to build PresentationRequest', error)
    }, 'Presentation', 'requestSession', [requestedUrl]);
  });

  ///////////////////////////////////////////////////////////////////////////////

  function startPresentation() {
    return new Promise(function(resolve, reject) {
      exec(function(result) {
        setupConnection(result);
        resolve(connection);
      }, function(error) {
        console.error(error);
        reject(error);
      }, 'Presentation', 'startSession', [connection.id]);
    });
  }

  function reconnectPresentation(id) {
    var connectionId = id || connection.id;
    return new Promise(function(resolve, reject) {
      exec(function(result) {
        setupConnection(result);
        resolve(connection);
      }, function(error) {
        console.error(error);
        reject(error);
      }, 'Presentation', 'reconnectSession', [connectionId]);
    });
  }

  function setupConnection(result) {
    connection.id = result.id;
    switch (result.eventType) {
      case 'onstatechange':
        handleStateChangeEvent(connection, result);
        break;
      case 'onmessage':
        if (typeof connection.onmessage === 'function') {
          var data = decodeURIComponent(result.value);
          var message;
          try {
            message = JSON.parse(data);
          } catch (e) {
            message = data;
          }
          connection.onmessage(message);
        }
        break;
      default:
        break;
    }
  }

  function handleStateChangeEvent(connection, result) {
    connection.state = result.value;
    switch (result.value) {
      case 'connected':
        connection.onconnect(connection);
        break;
      case 'connecting':
        break;
      case 'closed':
        var event = new PresentationConnectionCloseEvent('close', {message: result.message, reason: result.reason});
        connection.onclose(event);
        break;
      case 'terminated':
        connection.onterminate(connection);
        connection = undefined;
        break;
      default:
        console.error('Unknown connection state: ', result.value);
        break;
    }
  }

  function getPresentationAvailability() {
    presentationAvailability = new PresentationAvailability(connection);

    return new Promise(function(resolve, reject) {
      buildConnectionPromise.then(function() {
        resolve(presentationAvailability);
      }, function(error) {
        reject(error);
      });
    });
  }

  Object.defineProperty(this, 'start', {
    get: function() {
      return startPresentation;
    }
  });

  Object.defineProperty(this, 'reconnect', {
    get: function() {
      return reconnectPresentation;
    }
  });

  Object.defineProperty(this, 'getAvailability', {
    get: function() {
      return getPresentationAvailability;
    }
  });

  Object.defineProperty(this, 'onconnectionavailable', {
    get: function() {
      return onAvailableChange;
    },
    set: function(eventCallback) {
      if (typeof eventCallback === 'function' || eventCallback === null || eventCallback === undefined) {
        onAvailableChange = eventCallback;
        if (onAvailableChange) {
          var callback = function(result) {
            result.connection = connection;
            if (typeof onAvailableChange === 'function') {
              var evt = new PresentationConnectionAvailableEvent('connectionavailable', result);
              onAvailableChange(evt);
            }
          };
          exec(callback, function() {
          }, 'Presentation', 'addWatchAvailableChange', []);
        }
        else {
          //stop the service serving screen states
          exec(function() {
          }, function() {
          }, 'Presentation', 'clearWatchAvailableChange', []);
        }
      }
    }
  });
}

module.exports = PresentationRequest;
