package org.asanka.dev;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jaxen.JaxenException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by asanka on 3/7/16.
 */
public class PropertyTemplateMediator extends AbstractMediator implements ManagedLifecycle{

    Map xPathExpressions;
    String body;
    String propertyName;
    String scope;
    String mediaType;
    String targetType;
    VelocityEngine velocityEngine;

    public boolean mediate(MessageContext messageContext) {
        //evaluate values
        VelocityContext context = new VelocityContext();
        Iterator<Map.Entry<String,SynapseXPath>> xPathIterator = xPathExpressions.entrySet().iterator();
        while (xPathIterator.hasNext()){
            Map.Entry<String, SynapseXPath> next = xPathIterator.next();
            try {
                Object result = next.getValue().evaluate(messageContext);
                context.put(next.getKey(),result);
            } catch (JaxenException e) {
                //TODO:handler exception
                e.printStackTrace();
            }
        }
        StringWriter writer = new StringWriter();
        boolean propTempate = velocityEngine.evaluate(context, writer, "propTempate", new StringReader(this.getBody()));
        handleOutput(writer.toString(),messageContext);
        return true;
    }

    private void handleOutput(String result,MessageContext messageContext){
        TargetType targetType = TargetType.valueOf(this.targetType);
        switch (targetType){
            case body:
                //clean up body and add to the body
                handleBody(result,messageContext);
                break;
            case property:
                //add to the correct scope
                break;
            case envelop:
                //replace envelope
                break;
            case header:
                //add to soap header
                break;
            default:
                //add to body
                handleBody(result,messageContext);
                break;
        }
    }

    private void handleBody(String result,MessageContext messageContext) {
                  OMElement resultOM=null;
            //convert to xml and set to the body
            try {
                if(StringUtils.equals("xml",mediaType)) {
                    resultOM = AXIOMUtil.stringToOM(result);
                }else {
                    resultOM= JsonUtil.toXml(new ByteArrayInputStream(result.getBytes()), true);
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }
        SOAPBody body = messageContext.getEnvelope().getBody();
        PropertyTemplateUtils.cleanUp(body);
        body.addChild(resultOM);
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

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        velocityEngine=new VelocityEngine();
        velocityEngine.init();
    }

    @Override
    public void destroy() {

    }
}
