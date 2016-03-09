package org.asanka.dev;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by asanka on 3/7/16.
 */
public class PropertyTemplateMediatorSerializer extends AbstractMediatorSerializer {
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {
        if(!(mediator instanceof PropertyTemplateMediator)){
            handleException("Unsupported mediator passed in for serialization : "
                    + mediator.getType());
        }

        PropertyTemplateMediator propertyTemplateMediator=(PropertyTemplateMediator)mediator;
        OMElement mediatorRoot = fac.createOMElement(PropertyTemplateMediatorFactory.propertyTemplateElement);
        OMElement formatOmElement = fac.createOMElement(PropertyTemplateMediatorFactory.formatElement);
        OMElement formatBody=null;
        if(StringUtils.equals(propertyTemplateMediator.getMediaType(),"xml")){
            try {
                formatBody = AXIOMUtil.stringToOM(propertyTemplateMediator.getBody());
                formatOmElement.addChild(formatBody);
            } catch (XMLStreamException e) {
                handleException("Failed to serialize template format");
            }
        }else {
            formatOmElement.setText(propertyTemplateMediator.getBody());
        }

        mediatorRoot.addChild(formatOmElement);
        OMElement argsListElement = fac.createOMElement(PropertyTemplateMediatorFactory.argumentListElement);
        Iterator<Map.Entry<String,SynapseXPath>> iterator =propertyTemplateMediator.getxPathExpressions().entrySet().iterator();

        while (iterator.hasNext()){
            Map.Entry<String, SynapseXPath> next = iterator.next();
            OMElement arg = fac.createOMElement(PropertyTemplateMediatorFactory.argumentElement);
            arg.addAttribute(PropertyTemplateMediatorFactory.nameAttribute.getLocalPart(),next.getKey(),null);
            arg.addAttribute(PropertyTemplateMediatorFactory.expressionAttribute.getLocalPart(),next.getValue().getRootExpr().getText(),null);
            argsListElement.addChild(arg);
        }



        mediatorRoot.addChild(argsListElement);


        OMElement targetElement = fac.createOMElement(PropertyTemplateMediatorFactory.targetElement);
        targetElement.addAttribute(PropertyTemplateMediatorFactory.targetType.getLocalPart(),propertyTemplateMediator.
                getTargetType(),null);

        if(StringUtils.equals(propertyTemplateMediator.getTargetType(),"property")){
            targetElement.addAttribute(PropertyTemplateMediatorFactory.nameAttribute.getLocalPart(),propertyTemplateMediator.
                    getPropertyName(),null);
            targetElement.addAttribute(PropertyTemplateMediatorFactory.scopeAttribute.getLocalPart(),propertyTemplateMediator.
                    getScope(),null);
        }

        mediatorRoot.addChild(targetElement);
        return mediatorRoot;
    }

    public String getMediatorClassName() {
        return PropertyTemplateMediator.class.getName();
    }
}
