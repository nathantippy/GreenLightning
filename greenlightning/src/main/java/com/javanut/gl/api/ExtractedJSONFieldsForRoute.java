package com.javanut.gl.api;

import com.javanut.pronghorn.network.http.CompositeRoute;

public interface ExtractedJSONFieldsForRoute extends ExtractedJSONFields<ExtractedJSONFieldsForRoute> {

    CompositeRoute path(CharSequence path);
}
