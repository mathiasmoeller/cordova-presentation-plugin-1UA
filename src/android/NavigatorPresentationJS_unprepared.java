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
	public static String RECEIVER = "javascript:@@RECEIVER_MIN";
}
