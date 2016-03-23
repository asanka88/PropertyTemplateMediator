package org.asanka.dev;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.asanka.dev.enums.PropertyTypes;
import org.jaxen.JaxenException;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.Iterator;

/**
 * Created by asanka on 3/21/16.
 */
public class ArgXpath extends SynapseXPath {

    private PropertyTypes type;

    public PropertyTypes getType() {
        return type;
    }

    public void setType(PropertyTypes type) {
        this.type = type;
    }

    public Object getFormattedResult(MessageContext messageContext) throws JaxenException {
        Object evaluate = this.evaluate(messageContext);
        if(this.type==null){
            return evaluate;
        }else if(type==PropertyTypes.string){
            if(evaluate instanceof ArrayList){
                ArrayList tmpList=(ArrayList)evaluate;
                int size = tmpList.size();
                if(size >1){
                    ArrayList<String> result=new ArrayList<String>(tmpList.size());
                    Iterator iterator = tmpList.iterator();
                    while (iterator.hasNext()){
                        Object next = iterator.next();
                        result.add(getString(next));
                    }
                    return result;
                }else if(size==1){
                    Object o = tmpList.get(0);
                    return getString(o);
                }

            }

        }
        return evaluate;
    }
    private String getString(Object o){
        if(o instanceof OMElement){
            return ((OMElement) o).toString();
        }else if(o instanceof OMText){
            return ((OMText) o).getText();
        }else if(o instanceof OMAttribute){
            return ((OMAttribute) o).getAttributeValue();
        }else{
            return o.toString();
        }
    }

    public ArgXpath(String xpathString) throws JaxenException {
        super(xpathString);
    }

    public ArgXpath(String xpathString,String type) throws JaxenException {
        super(xpathString);
        if(StringUtils.isEmpty(type)){
            this.type=null;
        }else {
            this.type=PropertyTypes.valueOf(type);
        }

    }

}
