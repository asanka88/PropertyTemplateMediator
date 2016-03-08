import junit.framework.Assert;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.asanka.dev.PropertyTemplateMediator;
import org.asanka.dev.PropertyTemplateMediatorFactory;
import org.asanka.dev.PropertyTemplateMediatorSerializer;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Created by asanka on 3/7/16.
 */
public class FactoryTest {
@Test
public void factoryTest() throws XMLStreamException {

    String conf="<propertyTemplate name=\"outProp\" scope=\"default\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "<format>\n" +
            "  <person>\n" +
            "  \t<name>$name</name>\n" +
            "  \t<age>$age</age>\n" +
            "  </person>\n" +
            "</format>\n" +
            "<args>\n" +
            "\t<arg name=\"name\" expression=\"$ctx:name\"/>\n" +
            "<arg name=\"age\" expression=\"$ctx:age\"/>\n" +
            "</args>\n" +
            "</propertyTemplate>";

    OMElement confOMelemnt = AXIOMUtil.stringToOM(conf);
    PropertyTemplateMediatorFactory factory=new PropertyTemplateMediatorFactory();
    PropertyTemplateMediator mediator = (PropertyTemplateMediator) factory.createMediator(confOMelemnt, null);
    Assert.assertEquals(mediator.getPropertyName(),"outProp");


    PropertyTemplateMediatorSerializer serializer=new PropertyTemplateMediatorSerializer();
    OMElement root = OMAbstractFactory.getOMFactory().createOMElement(new QName("<root/>"));
    OMElement result = serializer.serializeMediator(null, mediator);
    System.out.printf(result.toString());

}

}
