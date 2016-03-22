import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.asanka.dev.VelocityTemplateMediator;
import org.asanka.dev.VelocityTemplateMediatorFactory;
import org.asanka.dev.VelocityMediatorSerializer;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Created by asanka on 3/7/16.
 */
public class FactoryTest {
@Test
public void factoryTestXML() throws XMLStreamException {

    String conf="<propertyTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "   <format>\n" +
            "      <person>\n" +
            "         <name>$name</name>\n" +
            "         <age>$age</age>\n" +
            "      </person>\n" +
            "   </format>\n" +
            "   <args>\n" +
            "      <arg name=\"name\" expression=\"$ctx:name\" />\n" +
            "      <arg name=\"age\" expression=\"$ctx:age\" />\n" +
            "   </args>\n" +
            "   <target type=\"property\" name=\"propertyName\" scope=\"propertyScope\" />\n" +
            "</propertyTemplate>";

    OMElement confOMelemnt = AXIOMUtil.stringToOM(conf);
    VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
    VelocityTemplateMediator mediator = (VelocityTemplateMediator) factory.createMediator(confOMelemnt, null);
    System.out.printf(mediator.getMediaType());
//    Assert.assertEquals(mediator.getPropertyName(),"outProp");


    VelocityMediatorSerializer serializer=new VelocityMediatorSerializer();
    OMElement root = OMAbstractFactory.getOMFactory().createOMElement(new QName("<root/>"));
    OMElement result = serializer.serializeMediator(null, mediator);
    System.out.printf(result.toString());

}


@Test
    public void factoryTestJSON() throws XMLStreamException {

        String conf="<propertyTemplate media-type=\"json\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "      {\"name\":$name,\n" +
                "      \"age\"\"$age\n" +
                "      }\n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"name\" expression=\"$ctx:name\" />\n" +
                "      <arg name=\"age\" expression=\"$ctx:age\" />\n" +
                "   </args>\n" +
                "   <target type=\"property\" name=\"propertyName\" scope=\"propertyScope\" />\n" +
                "</propertyTemplate>";

        OMElement confOMelemnt = AXIOMUtil.stringToOM(conf);
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        VelocityTemplateMediator mediator = (VelocityTemplateMediator) factory.createMediator(confOMelemnt, null);
        System.out.printf(mediator.getMediaType());
        System.out.printf(mediator.getBody());
//    Assert.assertEquals(mediator.getPropertyName(),"outProp");


    VelocityMediatorSerializer serializer=new VelocityMediatorSerializer();
    OMElement root = OMAbstractFactory.getOMFactory().createOMElement(new QName("<root/>"));
    OMElement result = serializer.serializeMediator(null, mediator);
    System.out.printf(result.toString());

    }

    @Test
    public void factoryTestXMLProperty() throws XMLStreamException {

        String conf="<velocityTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "      <person xmlns=\"\">\n" +
                "         <name>$name</name>\n" +
                "         <age>$age</age>\n" +
                "      </person>\n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"name\" expression=\"//name\" type=\"string\"/>\n" +
                "      <arg name=\"age\" expression=\"//age\" type=\"string\"/>\n" +
                "      <arg name=\"age1\" expression=\"$ctx:age\" type=\"string\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"body\"/>\n" +
                "</velocityTemplate>";

        OMElement confOMelemnt = AXIOMUtil.stringToOM(conf);
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        VelocityTemplateMediator mediator = (VelocityTemplateMediator) factory.createMediator(confOMelemnt, null);
        System.out.printf(mediator.getMediaType());
//    Assert.assertEquals(mediator.getPropertyName(),"outProp");


        VelocityMediatorSerializer serializer=new VelocityMediatorSerializer();
        OMElement root = OMAbstractFactory.getOMFactory().createOMElement(new QName("<root/>"));
        OMElement result = serializer.serializeMediator(null, mediator);
        System.out.printf(result.toString());

    }

    @Test
    public void test(){
    }


}
