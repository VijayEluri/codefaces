qx.Class.define("org.codefaces.httpclient.internal.ajax.AjaxClientWidget", {
  extend: qx.ui.core.Widget,
    
  construct: function() {
    this.base(arguments);
  },
  
  members: { 
    /**
     * send an JSONP request and send back the JSONP response
     *
     * @param[String] url - the HTTP URL.
     * @param[Number] timeout - timeout in milliseconds of each request. 
     *     Use default value if set to 0.
     */
    sendJsonpRequest: function(reqId, url, timeout) {
      //function for sending a reply to server
      var wid = org.eclipse.swt.WidgetManager.getInstance().findIdByWidget(this);
      var sendResponse = function(wid, status, content) {
      	var req = org.eclipse.swt.Request.getInstance();   
        req.addParameter(wid + '.requestId', reqId);
        req.addParameter(wid + '.status', status);
        if (content != null) {        	
        	req.addParameter(wid + '.content', content);
        }
        
        req.send();
      };
      
      // perform the call      
	  jQuery.jsonp({
	    url: url,
	    dataType: 'jsonp',
	    timeout: timeout,
	    callbackParameter: 'callback',
	    
	    //textStatus is always 'success'
	    success: function (data, textStatus) {
	      sendResponse(wid, textStatus, JSON.stringify(data));
	    },
	    
	    //textStatus can be 'error' or 'timeout'
	    error: function(xOptions, textStatus){
	      sendResponse(wid, textStatus, null);
	    }
	  });
	}
  }
});
