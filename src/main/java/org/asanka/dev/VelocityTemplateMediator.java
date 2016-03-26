package org.asanka.dev;

import com.mchange.v2.c3p0.impl.C3P0Defaults;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.AbstractOMMetaFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.*;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.saaj.SOAPFactoryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.SOAPUtils;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.AXIOMUtils;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.asanka.dev.enums.MediaTypes;
import org.asanka.dev.enums.PropertyTypes;
import org.asanka.dev.enums.Scopes;
import org.asanka.dev.enums.TargetType;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by asanka on 3/7/16.
 */
public class VelocityTemplateMediator extends AbstractMediator implements ManagedLifecycle{

    private static final Log LOG= LogFactory.getLog(VelocityTemplateMediator.class);
    public static final SOAP11Factory SOAP_11_FACTORY = new SOAP11Factory();
    public static final SOAP12Factory SOAP_12_FACTORY = new SOAP12Factory();
    public static final SOAPFactory SOAP_12_FACTORY1 = OMAbstractFactory.getSOAP12Factory();
    public static final SOAPFactory SOAP_11_FACTORY1 = OMAbstractFactory.getSOAP11Factory();
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
        Iterator<Map.Entry<String,ArgXpath>> xPathIterator = xPathExpressions.entrySet().iterator();
        while (xPathIterator.hasNext()){
            Map.Entry<String, ArgXpath> next = xPathIterator.next();
            ArgXpath xpath = null;
            try {
                xpath = next.getValue();
                Object result = xpath.getFormattedResult(messageContext);

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
        } catch (SOAPException e) {
            String msg = "Error while processing output to soap header";
            LOG.error(msg,e);
            handleException(msg,e,messageContext);
        }
        return true;
    }

    private void handleOutput(String result,MessageContext messageContext) throws XMLStreamException, AxisFault, SOAPException {
        switch (targetType){
            case body:
                //clean up body and add to the body
                handleBody(result,messageContext);
                break;
            case property:
                //add to the correct scope
                handleProperty(result,messageContext);
                break;
            case envelope:
                //replace envelope
                handleEnvelope(result,messageContext);
                break;
            case header:
                //add to soap header
                handleHeader(result,messageContext);
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


    private void handleHeader(String result,MessageContext messageContext) throws AxisFault, XMLStreamException, SOAPException {
        OMElement resultOM=null;
        //convert to xml and set to the body

        if(mediaType==MediaTypes.xml) {
            resultOM = AXIOMUtil.stringToOM(result);
            SOAPEnvelope envelope = messageContext.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            boolean soap11 = isSOAP11(envelope);
            if(header==null){
                if(soap11){
                    header= SOAP_11_FACTORY1.createSOAPHeader(envelope);
                }else{
                    header= SOAP_12_FACTORY1.createSOAPHeader(envelope);
                }
            }
            SOAPHeaderBlock headerBlock;
            if(soap11){
                headerBlock = SOAP_11_FACTORY1.createSOAPHeaderBlock(resultOM.getLocalName(),resultOM.getNamespace(),header);
            }else{
                headerBlock = SOAP_12_FACTORY1.createSOAPHeaderBlock(resultOM.getLocalName(),resultOM.getNamespace(),header);
            }
            Iterator<OMElement> children = resultOM.getChildElements();
            while (children.hasNext()){
                OMElement next = children.next();
                headerBlock.addChild(next);
            }


        }

    }


    private boolean isSOAP11(SOAPEnvelope envelope) {
        return (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI).equals(envelope.getNamespace().getNamespaceURI());
    }


    private void handleEnvelope(String result,MessageContext messageContext) throws AxisFault, XMLStreamException {
        OMElement resultOM=null;
        //convert to xml and set to the body

        if(mediaType==MediaTypes.xml) {
            resultOM = AXIOMUtil.stringToOM(result);
            QName firstChild = resultOM.getQName();
            if (firstChild.getLocalPart().equalsIgnoreCase("envelope") && (
                    firstChild.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) ||
                            firstChild.getNamespaceURI().
                                    equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))){
                //result is soap envelope
                SOAPEnvelope soapEnv = AXIOMUtils.getSOAPEnvFromOM(resultOM);
                if(soapEnv!=null){
                    soapEnv.buildWithAttachments();
                    messageContext.setEnvelope(soapEnv);
                }

            }else{
                String msg = "Result from Velocity template mediator is not an Envelope. Invalid target as envelope";
                LOG.error(msg);
                throw new SynapseException(msg);
            }
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
