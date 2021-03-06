/*
 * Copyright 2014 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * AUTHORS: Louay Bassbouss <louay.bassbouss@fokus.fraunhofer.de>
 *          Martin Lasak <martin.lasak@fokus.fraunhofer.de>
 */
package de.fhg.fokus.famium.presentation;

/**
 * 
 * This class collects all JavaScript Code that will be injected in WebViews. At the moment there is only one constant  <code>RECEIVER</code>. New contants my be added in future releases.
 *
 */
public class NavigatorPresentationJS {
	/**
	 * RECEIVER contains a minified version of the JavaScript file receiver.js. It will be injected in the WebView of the presenting page after page load finished.
	 */
	public static String RECEIVER = "javascript:function PresentationConnectionCloseEvent(n,e){this.type=n;var t=e.message,o=e.reason;Object.defineProperty(this,'message',{get:function(){return t}}),Object.defineProperty(this,'reason',{get:function(){return o}})}function PresentationConnectionAvailableEvent(n,e){this.type=n;var t=e.connection,o=e.available;Object.defineProperty(this,'connection',{get:function(){return t}}),Object.defineProperty(this,'value',{get:function(){return o}})}!function(n){function e(){Object.defineProperty(this,'connectionList',{get:function(){return f}})}function t(n){var e=null,n=n;Object.defineProperty(this,'connections',{get:function(){return n}}),Object.defineProperty(this,'onconnectionavailable',{get:function(){return e},set:function(n){'function'!=typeof n&&null!==n||(e=n)}})}function o(n){Object.defineProperty(this,'state',{get:function(){return n&&n.state||null}}),Object.defineProperty(this,'id',{get:function(){return n&&n.id||null}}),Object.defineProperty(this,'url',{get:function(){return n&&n.url||null}}),Object.defineProperty(this,'onmessage',{get:function(){return n.onmessage},set:function(e){'function'!=typeof e&&null!==e||(n.onmessage=e)}}),Object.defineProperty(this,'onconnect',{get:function(){return n.onconnect},set:function(e){'function'!=typeof e&&null!==e||(n.onconnect=e)}}),Object.defineProperty(this,'onclose',{get:function(){return n.onclose},set:function(e){'function'!=typeof e&&null!==e||(n.onclose=e)}}),Object.defineProperty(this,'onterminate',{get:function(){return n.onterminate},set:function(e){'function'!=typeof e&&null!==e||(n.onterminate=e)}}),Object.defineProperty(this,'send',{get:function(){return function(e){return n.send(e)}}}),Object.defineProperty(this,'close',{get:function(){return function(e,t){return n.close(e,t)}}}),Object.defineProperty(this,'terminate',{get:function(){return function(){return n.terminate()}}})}var i=function(){var n=new e;Object.defineProperty(this,'receiver',{get:function(){return n}})},r=new i;Object.defineProperty(window.navigator,'presentation',{get:function(){return r}});var c=[],u=new t(c),f=new Promise(function(e){n.onpresent=function(n){var t=new o(n);if(c.push(t),e(u),u.onconnectionavailable){var i=new PresentationConnectionAvailableEvent('connectionavailable',{connection:n,value:!0});u.onconnectionavailable(i)}}})}(function(n){function e(e,t,o,i){switch(e.state=t,t){case'connected':e.onconnect(e);break;case'connecting':n.setOnPresent();break;case'closed':var r=new PresentationConnectionCloseEvent('close',{message:i,reason:o});e.onclose(r);break;case'terminated':e.onterminate(e),e=void 0;break;default:console.error('Unknown connection state: ',t)}}var t={},o=function(){var e=null;Object.defineProperty(this,'onpresent',{get:function(){return e},set:function(t){'function'!=typeof t&&'undefined'!=typeof t&&null!==t||(e=t,e&&n.setOnPresent())}})};n.onsession=function(e){t[e.id]=t[e.id]||e;var o=function(){},r=function(){},c=function(){},u=function(){};Object.defineProperty(e,'onmessage',{get:function(){return o},set:function(n){'function'!=typeof n&&'undefined'!=typeof n&&null!==n||(o=n)}}),Object.defineProperty(e,'onconnect',{get:function(){return r},set:function(n){'function'!=typeof n&&'undefined'!=typeof n&&null!==n||(r=n)}}),Object.defineProperty(e,'onclose',{get:function(){return c},set:function(n){'function'!=typeof n&&'undefined'!=typeof n&&null!==n||(c=n)}}),Object.defineProperty(e,'onterminate',{get:function(){return u},set:function(n){'function'!=typeof n&&'undefined'!=typeof n&&null!==n||(u=n)}}),Object.defineProperty(e,'send',{get:function(){return function(t){var o=encodeURIComponent(JSON.stringify(t));return n.postMessage(e.id,o)}}}),Object.defineProperty(e,'close',{get:function(){return function(t,o){return n.close(e.id,t,o)}}}),Object.defineProperty(e,'terminate',{get:function(){return function(){return n.terminate(e.id)}}}),i.onpresent&&i.onpresent(e)},n.onmessage=function(n,e){var o,i=t[n],r=decodeURIComponent(e);try{o=JSON.parse(r)}catch(c){o=r}i&&i.onmessage&&i.onmessage.call(null,o)},n.onstatechange=function(n,o,i,r){var c=t[n];c&&e(c,o,i,r)};var i=new o;return i}(NavigatorPresentationJavascriptInterface)),PresentationConnectionCloseEvent.prototype=Event.prototype,PresentationConnectionAvailableEvent.prototype=Event.prototype;";
}
