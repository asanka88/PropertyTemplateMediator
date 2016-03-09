package org.asanka.dev;

import com.jayway.jsonpath.JsonPath;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.AXIOMUtils;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jaxen.JaxenException;

import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by asanka on 3/7/16.
 */
public class PropertyTemplateMediator extends AbstractMediator{

    Map xPathExpressions;
    String body;
    String propertyName;
    String scope;
    String mediaType;
    String targetType;


    public boolean mediate(MessageContext messageContext) {
        //evaluate values
        Map<String,Object> evaluatedValues=new HashMap<String, Object>();
        VelocityContext context = new VelocityContext();
        Iterator<Map.Entry<String,SynapseXPath>> xPathIterator = xPathExpressions.entrySet().iterator();
        while (xPathIterator.hasNext()){
            Map.Entry<String, SynapseXPath> next = xPathIterator.next();
            try {
                Object result = next.getValue().evaluate(messageContext);
                context.put(next.getKey(),result);
            } catch (JaxenException e) {
                e.printStackTrace();
            }

        }


        //replace body using velocity template engine
        VelocityEngine ve = new VelocityEngine();

        ve.init();
        StringWriter writer = new StringWriter();
        OMElement resultOM=null;
        boolean propTempate = ve.evaluate(context, writer, "propTempate", new StringReader(this.getBody()));
        try {
            resultOM = AXIOMUtil.stringToOM(writer.toString());
        } catch (XMLStreamException e) {


        }
        if(!propTempate && resultOM!=null){
            throw new SynapseException("Failed at template evaluation");
        }else{
            //output porerty name //scope
            //TODO Implement to support other scopes
            messageContext.setProperty(this.getPropertyName(),resultOM);
        }

        return true;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Map getxPathExpressions() {
        return this.xPathExpressions;
    }

    public void setxPathExpressions(Map xPathExpressions) {
        this.xPathExpressions = xPathExpressions;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
