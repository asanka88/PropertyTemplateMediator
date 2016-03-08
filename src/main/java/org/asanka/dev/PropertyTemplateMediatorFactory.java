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
import java.util.*;

/**
 * Created by asanka on 3/7/16.
 */
public class PropertyTemplateMediatorFactory extends AbstractMediatorFactory {
    public static final QName propertyTemplateElement=new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"propertyTemplate");
    public static final QName formatElement=new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"format");
    public static final QName argumentListElement =new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"args");
    public static final QName argumentElement =new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"arg");
    public static final QName expressionAttribute=new QName("expression");
    public static final QName nameAttribute =new QName("name");
    public static final QName scopeAttribute=new QName("scope");

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        PropertyTemplateMediator mediator=new PropertyTemplateMediator();
        String propertyName = omElement.getAttributeValue(nameAttribute);
        if(StringUtils.isEmpty(propertyName)){
            throw new SynapseArtifactDeploymentException("name is empty in PropertyTemplate Mediator");
        }
        mediator.setPropertyName(propertyName);
        String scope = omElement.getAttributeValue(scopeAttribute);
        if(StringUtils.isEmpty(scope)) {
            scope="default";
        }
        mediator.setScope(scope);
        OMElement format = omElement.getFirstChildWithName(formatElement);
        OMElement templateBody = format.getFirstElement();
        if(templateBody ==null){
            throw new SynapseArtifactDeploymentException("Template format is empty in PropertyTemplate Mediator");
        }

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
        mediator.setBody(templateBody.toString());
        return mediator;
    }

    public QName getTagQName() {
        return propertyTemplateElement;
    }
}
