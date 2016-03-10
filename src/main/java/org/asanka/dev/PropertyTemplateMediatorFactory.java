package org.asanka.dev;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.deployers.SynapseArtifactDeploymentException;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by asanka on 3/7/16.
 */
public class PropertyTemplateMediatorFactory extends AbstractMediatorFactory {
    public static final QName propertyTemplateElement=new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"propertyTemplate");
    public static final QName formatElement=new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"format");
    public static final QName argumentListElement =new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"args");
    public static final QName argumentElement =new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"arg");
    public static final QName targetElement =new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"target");
    public static final QName expressionAttribute=new QName("expression");
    public static final QName nameAttribute =new QName("name");
    public static final QName scopeAttribute=new QName("scope");
    public static final QName propertyTypeAttribute=new QName("property-type");
    public static final QName mediaTypeAttribute=new QName("media-type");
    public static final QName targetType=new QName("target-type");



    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        PropertyTemplateMediator mediator=new PropertyTemplateMediator();
        String mediaTypeAttrValue = omElement.getAttributeValue(mediaTypeAttribute);
        String mediaType = StringUtils.isEmpty(mediaTypeAttrValue)?"xml":mediaTypeAttrValue;
        mediator.setMediaType(mediaType);//setting media type
        OMElement format = omElement.getFirstChildWithName(formatElement);
        if(format == null || (StringUtils.equals("xml",mediaType)&& format.getFirstElement()==null) ||
                (StringUtils.equals("json",mediaType)&& StringUtils.isEmpty(format.getText()))){
            //meets failure condition
            //format element is null or
            //if xml this doesn't have xml template body or
            //if json this doesn't have json string
            throw new SynapseArtifactDeploymentException("Template format is empty in PropertyTemplate Mediator");
        }
        //if media type is xml then the template body is first element of the format element
        //other wise it is json, then it is json string wrapped by format element
        String templateBody=(StringUtils.equals("xml",mediaType))?format.getFirstElement().toString():format.getText();
        mediator.setBody(templateBody);

        OMElement argumentList = omElement.getFirstChildWithName(argumentListElement);
        Iterator<OMElement> argumentsIterator = argumentList.getChildrenWithName(argumentElement);
        Map<String,SynapseXPath> synXpathMap= new HashMap<String, SynapseXPath>();
        while(argumentsIterator.hasNext()){
            OMElement argument = argumentsIterator.next();
            String name = argument.getAttributeValue(nameAttribute);
            String attributeValue = argument.getAttributeValue(expressionAttribute);
            if(StringUtils.isEmpty(attributeValue) || StringUtils.isEmpty(name)){
                throw new SynapseArtifactDeploymentException("expression or name attribute is missing in the arg element");
            }
            try {
                synXpathMap.put(name,new SynapseXPath(attributeValue));
            } catch (JaxenException e) {
                e.printStackTrace();
            }
        }

        mediator.setxPathExpressions(synXpathMap);

        OMElement targetEle = omElement.getFirstChildWithName(targetElement);
        if(targetElement==null){
            throw new SynapseArtifactDeploymentException("Target element is missing in the Template Mediator");
        }
        String targetTypeValue =targetEle.getAttributeValue(targetType);
        targetTypeValue=(StringUtils.isEmpty(targetTypeValue))?"body":targetTypeValue;
        mediator.setTargetType(targetTypeValue);
        if(StringUtils.equalsIgnoreCase("property",targetTypeValue)){
            //if the target type is property then property name is mandotary
            String propertyName = targetEle.getAttributeValue(nameAttribute);
            String propertyType = targetEle.getAttributeValue(propertyTypeAttribute);

            if(StringUtils.isEmpty(propertyName)){
                throw new SynapseArtifactDeploymentException("property name attribute is required in Template Mediator," +
                        " when the type is property");
            }
            String scope = targetEle.getAttributeValue(scopeAttribute);
            scope=(StringUtils.isEmpty(scope))?"synapse":scope;
            propertyType=(StringUtils.isEmpty(propertyType))?"string":propertyType;
            mediator.setPropertyName(propertyName);
            mediator.setScope(scope);
            mediator.setPropertyType(propertyType);
        }
        return mediator;
    }

    public QName getTagQName() {
        return propertyTemplateElement;
    }
}
