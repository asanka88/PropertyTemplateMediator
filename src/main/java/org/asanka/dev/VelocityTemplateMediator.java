package org.asanka.dev;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG= LogFactory.getLog(VelocityTemplateMediator.class);
    private Map xPathExpressions;
    private String body;
    private String propertyName;
    private Scopes scope;
    private MediaTypes mediaType;
    private TargetType targetType;
    private PropertyTypes propertyType;
    private VelocityEngine velocityEngine;

    public boolean mediate(MessageContext messageContext) {
        //evaluate values
        if(LOG.isDebugEnabled()){
            LOG.debug("Velocity Template mediator started for "+messageContext.getMessageID());
        }

        VelocityContext context = new VelocityContext();
        Iterator<Map.Entry<String,SynapseXPath>> xPathIterator = xPathExpressions.entrySet().iterator();
        while (xPathIterator.hasNext()){
            Map.Entry<String, SynapseXPath> next = xPathIterator.next();
            SynapseXPath xpath = null;
            try {
                xpath = next.getValue();
                Object result = xpath.evaluate(messageContext);
                if(LOG.isDebugEnabled()){
                    String msg=String.format("Argument %s result== %s",xpath.getRootExpr().getText(),result.toString());
                    LOG.debug(msg);
                }
                context.put(next.getKey(),result);
            } catch (JaxenException e) {
                String msg = String.format("Error while evaluating argument %s",xpath.getRootExpr().getText());
                LOG.error(msg,e);
                handleException(msg,e,messageContext);
            }
        }
        StringWriter writer = new StringWriter();
        boolean propTempate = velocityEngine.evaluate(context, writer, "propTempate", new StringReader(this.getBody()));
        try {
            String result = writer.toString();
            if(LOG.isDebugEnabled()){
                LOG.debug(":::Resulted output from template:::");
                LOG.debug(result);
            }
            handleOutput(result,messageContext);
        } catch (XMLStreamException e) {
            String msg = "Error while processing output to the destination";
            LOG.error(msg,e);
            handleException(msg,e,messageContext);
        } catch (AxisFault e) {
            String msg = "Error while processing output to message body";
            LOG.error(msg,e);
            handleException(msg,e,messageContext);
        }
        return true;
    }

    private void handleOutput(String result,MessageContext messageContext) throws XMLStreamException, AxisFault {
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

    private void handleProperty(String result, MessageContext messageContext) throws XMLStreamException {
        Object formattedProperty = getFormattedProperty(result, propertyType);
        if(LOG.isDebugEnabled()){
            String msg = String.format("Target type:: property , scope %s",this.scope);
            LOG.debug(msg);
        }
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

    private Object getFormattedProperty(String result, PropertyTypes propertyType) throws XMLStreamException {
        switch (propertyType){
            case string:
                return result;
            case om:
                    OMElement omElement = AXIOMUtil.stringToOM(result);
                    return omElement;
        }
        return null;
    }


    private void handleBody(String result,MessageContext messageContext) throws AxisFault, XMLStreamException {
                  OMElement resultOM=null;
            //convert to xml and set to the body

            if(mediaType==MediaTypes.xml) {
                resultOM = AXIOMUtil.stringToOM(result);
            }else {
                resultOM= JsonUtil.toXml(new ByteArrayInputStream(result.getBytes()), true);
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
        if(LOG.isDebugEnabled()){
            LOG.debug("Initalizing Velocity Engine");
        }
        velocityEngine=new VelocityEngine();
        velocityEngine.init();
    }

    @Override
    public void destroy() {

    }
}
