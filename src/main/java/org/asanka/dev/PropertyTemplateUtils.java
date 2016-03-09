package org.asanka.dev;

import org.apache.axiom.om.OMElement;

import java.util.Iterator;

/**
 * Created by asanka on 3/8/16.
 */
public class PropertyTemplateUtils {
    public static void cleanUp(OMElement om){
        Iterator<OMElement> iterator = om.getChildElements();
        while (iterator.hasNext()){
            iterator.next().detach();
        }
    }
}
