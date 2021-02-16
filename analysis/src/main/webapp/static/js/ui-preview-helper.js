// Start of user code "Copyright Header"
/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Simple
 *
 * This file is generated by Lyo Designer (https://www.eclipse.org/lyo/)
 */
// End of user code

//Setup a popover on each of the oslcLinkElements, where the popover content is an iframe presenting the OSLC UI-Preview.
function setupUiPreviewOnPopover(oslcLinkElements) {
    oslcLinkElements.popover({
        container: "body",
        content: "Loading...",
        delay: {"show": 120, "hide": 60},
        html: true,
        placement: "auto",
        trigger: "hover"
    });

    oslcLinkElements.on("show.bs.popover", function () {
        var uiElem = $(this);
        var popoverElem = uiElem.data('bs.popover');
        getUiPreviewIframe(this.getAttribute("href"), attachIframeToHyperlinkElement, uiElem);
    })
}

function attachIframeToHyperlinkElement(iframeTitle, iframeHtml, resourceCompactStructure, uiElem) {
    uiElem.attr('data-original-title', iframeTitle);
    uiElem.attr('data-content', iframeHtml);
    uiElem.data('bs.popover').setContent();
}

//Perform an asynch GET request to obtain the resource's UI-Preview information (an OSLC Compact resource).
//callbackFunction is then called once the request response is obtained.
//The caller should supply this callbackFunction, with any desired paramters under "callbackParamter" 
//callbackFunction will be called with the following paramters (a) iframeTitle, (b) iframeHtml, (c) compactStructure, (d) callbackParamter
//where compactStructure represents more detailed about the OSLC Compact resource.
function getUiPreviewIframe(resourceUrl, callbackFunction, callbackParamter) {
    xmlhttp = new XMLHttpRequest();
    xmlhttp.onload = function () {
        if (this.status==200) {
            data = this.responseText;
            try {
                var parser = new DOMParser();
                var xmlDoc = parser.parseFromString(data,"text/xml");
                var compactStructure = parseOslcCompactXmlDocument(xmlDoc);
                var w = compactStructure.width ? compactStructure.width : "45em";
                var h = compactStructure.height ? compactStructure.height : "11em";
                var iframeHtml = "<iframe src='" + compactStructure.uri + "' ";
                iframeHtml += " style='border:0px; height:" + h + "; width:" + w + "'";
                iframeHtml += "></iframe>";
                callbackFunction(compactStructure.title, iframeHtml, compactStructure, callbackParamter);
            } catch (e) {
                iframeHtml = "<b>Error parsing preview dialog info</b>";
                callbackFunction("Error", iframeHtml, null, callbackParamter);
            }
        }
        else {
            iframeHtml = "<b>Error loading the preview dialog</b> status:" + this.status;
            callbackFunction("Error", iframeHtml, null, callbackParamter);
        }
    };
    xmlhttp.open("GET", resourceUrl, true);
    xmlhttp.setRequestHeader("Accept", "application/x-oslc-compact+xml");
    xmlhttp.send();
}

//returns a JSON struct representing a large or small UI-Preview info (uri, title, height and width) based on an OSLC Compact RDF resource.
function parseOslcCompactXmlDocument(oslcCompactXmlDocument) {
    var compactStructure = {};
    var compact = findFirstChildNode(findFirstChildNode(oslcCompactXmlDocument));

    var titleChild = findFirstChildNodeNamed(compact, 'dcterms:title');
    compactStructure.title = titleChild.textContent;

    var smallPrev = findFirstChildNodeNamed(compact, 'oslc:smallPreview');
    var largePrev = findFirstChildNodeNamed(compact, 'oslc:largePreview');
    var preview;
    if (smallPrev !== null) {
        preview = findFirstChildNode(smallPrev);
    } else {
        preview = findFirstChildNode(largePrev);
    }
    if (preview) {
        var document = findFirstChildNodeNamed(preview, 'oslc:document');
        if (document) compactStructure.uri = document.getAttribute('rdf:resource');
        var height = findFirstChildNodeNamed(preview, 'oslc:hintHeight');
        compactStructure.height = height.textContent;
        var width = findFirstChildNodeNamed(preview, 'oslc:hintWidth');
        compactStructure.width = width.textContent;
    }
    return compactStructure;
}

function findFirstChildNode(e) {
    for (i = 0; i < e.childNodes.length; i++) {
        if (e.childNodes[i].nodeType === Node.ELEMENT_NODE) {
            return e.childNodes[i];
        }
    }
}

function findFirstChildNodeNamed(e, nodeName) {
    for (i = 0; i < e.childNodes.length; i++) {
        if (e.childNodes[i].nodeType === Node.ELEMENT_NODE
                && e.childNodes[i].nodeName === nodeName) {
            return e.childNodes[i];
        }
    }
}