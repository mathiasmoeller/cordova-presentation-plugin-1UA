var CONNECTING = 'connecting';
var CONNECTED = 'connected';
var CLOSED = 'closed';
var TERMINATED = 'terminated';

function PresentationConnection(url) {
  var id;
  var state = CLOSED;
  var url = url;

  var onconnect = function () {};
  var onclose = function () {};
  var onterminate = function () {};
  var onmessage = function () {};

  Object.defineProperty(this, 'id', {
    get: function() {
      return id;
    },
    set: function(newId) {
      id = newId;
    }
  });

  Object.defineProperty(this, 'url', {
    get: function() {
      return url;
    },
    set: function(newUrl) {
      url = newUrl;
    }
  });

  Object.defineProperty(this, 'state', {
    get: function() {
      return state;
    },
    set: function(newState) {
      state = newState;
    }
  });

  Object.defineProperty(this, 'close', {
    get: function() {
      return closeConnection(this);
    }
  });

  Object.defineProperty(this, 'terminate', {
    get: function() {
      return terminateConnection(this);
    }
  });

  Object.defineProperty(this, 'send', {
    get: function() {
      return sendMessage(this);
    }
  });

  Object.defineProperty(this, 'onmessage', {
    get: function() {
      return onmessage;
    },
    set: function(value) {
      if (typeof value === 'function' || value === null) {
        onmessage = value;
      }
    }
  });

  Object.defineProperty(this, 'onconnect', {
    get: function() {
      return onconnect;
    },
    set: function(value) {
      if (typeof value === 'function' || value === null) {
        onconnect = value;
      }
    }
  });

  Object.defineProperty(this, 'onclose', {
    get: function() {
      return onclose;
    },
    set: function(value) {
      if (typeof value === 'function' || value === null) {
        onclose = value;
      }
    }
  });

  Object.defineProperty(this, 'onterminate', {
    get: function() {
      return onterminate;
    },
    set: function(value) {
      if (typeof value === 'function' || value === null) {
        onterminate = value;
      }
    }
  });

  var sendMessage = function(connection) {
    return function(message) {
      var encodedMessage = encodeURIComponent(JSON.stringify(message));
      exec(/*successCallback*/Function, /*errorCallback*/Function, 'Presentation', 'presentationSessionPostMessage', [connection.id, encodedMessage]);
    };
  };

  var closeConnection = function(connection) {
    return function(message) {
      exec(/*successCallback*/Function, /*errorCallback*/Function, 'Presentation', 'presentationSessionClose', [connection.id, CLOSED, message]);
    };
  };

  var terminateConnection = function(connection) {
    return function() {
      exec(/*successCallback*/Function, /*errorCallback*/Function, 'Presentation', 'presentationSessionTerminate', [connection.id]);
    };
  };
}

// interface PresentationConnectionAvailableEvent ////////////////////////////

function PresentationConnectionAvailableEvent(type, eventInitDict) {
  this.type = type;
  var connection = eventInitDict.connection;
  var value = eventInitDict.available;
  Object.defineProperty(this, 'connection', {
    get: function() {
      return connection;
    }
  });
  Object.defineProperty(this, 'value', {
    get: function() {
      return value;
    }
  });
}

PresentationConnectionAvailableEvent.prototype = Event.prototype;

// interface PresentationConnectionCloseEvent ////////////////////////////

function PresentationConnectionCloseEvent(type, eventInitDict) {
  this.type = type;
  var message = eventInitDict.message;
  var reason = eventInitDict.reason;

  Object.defineProperty(this, 'message', {
    get: function() {
      return message;
    }
  });

  Object.defineProperty(this, 'reason', {
    get: function() {
      return reason;
    }
  });
}

PresentationConnectionCloseEvent.prototype = Event.prototype;