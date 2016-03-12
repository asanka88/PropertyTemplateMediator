package org.asanka.dev;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.asanka.dev.enums.MediaTypes;
import org.asanka.dev.enums.PropertyTypes;
import org.asanka.dev.enums.Scopes;
import org.asanka.dev.enums.TargetType;
import org.jaxen.JaxenException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by asanka on 3/7/16.
 */
public class VelocityTemplateMediator extends AbstractMediator implements ManagedLifecycle{

    Map xPathExpressions;
    String body;
    String propertyName;
    Scopes scope;
    MediaTypes mediaType;
    TargetType targetType;
    PropertyTypes propertyType;
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
        switch (targetType){
            case body:
                //clean up body and add to the body
                handleBody(result,messageContext);
                break;
            case property:
                //add to the correct scope
                handleProperty(result,messageContext);
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

    private void handleProperty(String result, MessageContext messageContext) {
        Object formattedProperty = getFormattedProperty(result, propertyType);
        switch (scope){
            case synapse:
                messageContext.setProperty(this.propertyName,formattedProperty);
                break;
            case axis2:
                ((Axis2MessageContext)messageContext).getAxis2MessageContext().setProperty(this.propertyName,
                        formattedProperty);
                break;
            case operation:
                ((Axis2MessageContext)messageContext).getAxis2MessageContext().getOperationContext().setProperty(
                        this.propertyName,formattedProperty);
                break;
        }
    }

    private Object getFormattedProperty(String result, PropertyTypes propertyType) {
        switch (propertyType){
            case string:
                return result;
            case om:
                try {
                    OMElement omElement = AXIOMUtil.stringToOM(result);
                    return omElement;
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
                break;

        }
        return null;
    }


    private void handleBody(String result,MessageContext messageContext) {
                  OMElement resultOM=null;
            //convert to xml and set to the body
            try {
                if(mediaType==MediaTypes.xml) {
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

    public String getPropertyType() {
        return propertyType.toString();
    }

    public void setPropertyType(String propertyType) {
        try {
            this.propertyType=PropertyTypes.valueOf(propertyType.toLowerCase());
        }catch (IllegalArgumentException ex){
            throw new SynapseArtifactDeploymentException("Unsupported property type");
        }
    }

    public String getTargetType() {
        return targetType.toString();
    }

    public void setTargetType(String targetType) {

        try {
            this.targetType=TargetType.valueOf(targetType.toLowerCase());
        }catch (IllegalArgumentException ex){
            throw new SynapseArtifactDeploymentException("Unsupported target type");
        }

    }

    public String getMediaType() {
        return mediaType.toString();
    }

    public void setMediaType(String mediaType) {
        try {
            this.mediaType=MediaTypes.valueOf(mediaType.toLowerCase());
        }catch (IllegalArgumentException ex){
            throw new SynapseArtifactDeploymentException("Unsupported media type");
        }

    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getScope() {
        return scope.toString();
    }

    public void setScope(String scope) {

        try {
            this.scope=Scopes.valueOf(scope.toLowerCase());
        }catch (IllegalArgumentException ex){
            throw new SynapseArtifactDeploymentException("Unsupported scope type");
        }

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
