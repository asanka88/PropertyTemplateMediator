import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.asanka.dev.VelocityTemplateMediator;
import org.asanka.dev.VelocityTemplateMediatorFactory;
import org.asanka.dev.enums.Scopes;
import org.asanka.dev.enums.TargetType;
import org.asanka.dev.testUtils.TestUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Created by asanka on 3/21/16.
 */
public class VelocityMediatorXMLTestCase extends XMLTestCase{

    public VelocityMediatorXMLTestCase() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public static final String OUTPUT_PROPERTY = "outputProperty";


    public void testOneToOneXMLSourceBody_target_Body()throws Exception{

        MessageContext messageContext = OneToOneXMLSourceBody(TargetType.body, null);
        OMElement body1 = messageContext.getEnvelope().getBody().getFirstElement();
        String expected="<person><name>asanka</name><age>27</age><name>testcode</name></person>";
        String result = body1.toString();
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
        assertXMLEqual(expected,result);
    }

    public void testOneToOneXMLSourceBody_target_default_Target()throws Exception{

        MessageContext messageContext = OneToOneXMLSourceBody(null, null);
        OMElement body1 = messageContext.getEnvelope().getBody().getFirstElement();
        String expected="<person><name>asanka</name><age>27</age><name>testcode</name></person>";
        String result = body1.toString();
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
        assertXMLEqual(result,expected);
    }

    public void testOneToOneXMLSourceBody_target_property_default()throws Exception{

        MessageContext messageContext = OneToOneXMLSourceBody(TargetType.property,Scopes.synapse);
        OMElement body1 = (OMElement) messageContext.getProperty(OUTPUT_PROPERTY);
        String expected="<person><name>asanka</name><age>27</age><name>testcode</name></person>";
        String result = body1.toString();
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
        assertXMLEqual(result,expected);
    }
//
//    public void testOneToOneXMLSourceBody_target_property_operation()throws Exception{
//
//        MessageContext messageContext = OneToOneXMLSourceBody(TargetType.property,Scopes.operation);
//        OMElement body1 = (OMElement) ((Axis2MessageContext)messageContext).getAxis2MessageContext().
//                getOperationContext().getProperty(OUTPUT_PROPERTY);
//        String expected="<person><name>asanka</name><age>27</age><name>testcode</name></person>";
//        String result = removeWhiteSpaces(body1.toString());
////        System.out.println("=========================================");
////        System.out.println(result);
////        System.out.println("=========================================");
//        assertEquals(result,expected);
//    }

    public void testOneToOneXMLSourceBody_target_property_axis2()throws Exception{

        MessageContext messageContext = OneToOneXMLSourceBody(TargetType.property,Scopes.axis2);
        OMElement body1 = (OMElement) ((Axis2MessageContext)messageContext).getAxis2MessageContext().getProperty(OUTPUT_PROPERTY);
        String expected="<person><name>asanka</name><age>27</age><name>testcode</name></person>";
        String result =body1.toString();
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
        assertXMLEqual(result,expected);
    }

    public MessageContext OneToOneXMLSourceBody(TargetType target, Scopes scope) throws Exception {
        String body="<people>" +
                    "<person>" +
                    "<name>asanka</name>" +
                    "<age>27</age>" +
                    "</person>" +
                    "</people>";

        StringBuilder mediator=new StringBuilder("<velocityTemplate media-type=\"xml" +
                "\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "      <person xmlns=\"\">\n" +
                "         <name>$name</name>\n" +
                "         <age>$age</age>\n" +
                "         <name>$code</name>\n" +
                "      </person>\n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"name\" expression=\"//name/text()\" type=\"string\"/>\n" +
                "      <arg name=\"age\" expression=\"//age/text()\" type=\"string\"/>\n" +
                "      <arg name=\"code\" expression=\"$ctx:code\"/>\n" +
                "   </args>\n");
        if(target!=null){
            mediator.append("<target target-type=\""+target.toString()+"\"");
        }else {
            mediator.append("<target property-type=\"om\"/>");

        }
        if(target==TargetType.property){
            mediator.append(" name=\""+OUTPUT_PROPERTY+"\" scope=\""+scope.toString()+"\" property-type=\"om\"/>\n");
        }else{
            mediator.append(" type=\"om\"/>\n");

        }
                mediator.append("</velocityTemplate>");

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        messageContext.setProperty("code","testcode");
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator.toString()), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator templateMediator=(VelocityTemplateMediator)mediator1;
        templateMediator.init(null);
        boolean mediate = mediator1.mediate(messageContext);
        assertTrue(mediate);//mediation is successfull
        return messageContext;
    }

    public void testOneToOneXMLSourceBodyTargetEnvelope() throws Exception {
        String expected="<?xml version='1.0' encoding='utf-8'?><sopaenv:Envelope xmlns:sopaenv=\"http://schemas.xmlsoap.org/soap/envelope/\" sopaenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><sopaenv:Body>\n" +
                "       \n" +
                "   <sopaenv:Envelope sopaenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "   <sopaenv:Body>\n" +
                "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" +
                "           <symbol>testcode</symbol>\n" +
                "       </m:GetLastTradePrice>\n" +
                "   </sopaenv:Body>\n" +
                "</sopaenv:Envelope></sopaenv:Body></sopaenv:Envelope>";
        String body="\n" +
                "<people>\n" +
                "    <person>\n" +
                "      <name>asanka</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>nuwan</name>\n" +
                "      <age>28</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Eranda</name>\n" +
                "      <age>30</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Malith</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "</people>";

        StringBuilder mediator=new StringBuilder("<velocityTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "     <sopaenv:Envelope\n" +
                "  xmlns:sopaenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "  sopaenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "   <sopaenv:Body>\n" +
                "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" +
                "           <symbol xmlns=\"\">$code</symbol>\n" +
                "       </m:GetLastTradePrice>\n" +
                "   </sopaenv:Body>\n" +
                "</sopaenv:Envelope> \n" +
                "      \n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"code\" expression=\"$ctx:code\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"envelope\"/>\n" +
                "</velocityTemplate>");

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        messageContext.setProperty("code","testcode");
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator.toString()), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator templateMediator=(VelocityTemplateMediator)mediator1;
        templateMediator.init(null);
        boolean mediate = mediator1.mediate(messageContext);
        assertTrue(mediate);//mediation is successfull
        OMElement body1 =messageContext.getEnvelope();
        String result = body1.toString();
//        System.out.println("=========================================");
//        System.out.println(body1.toString());
//        System.out.println("=========================================");
        assertXMLEqual(result,expected);
    }

    public void testOneToOneXMLSourceBodyTargetHeader() throws Exception {
        String expected="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header><person><name>testcode</name></person></soapenv:Header><soapenv:Body><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "   <SOAP-ENV:Body>\n" +
                "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" +
                "           <symbol>DIS</symbol>\n" +
                "       </m:GetLastTradePrice>\n" +
                "   </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope></soapenv:Body></soapenv:Envelope>";
        String body="<SOAP-ENV:Envelope\n" +
                "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "   <SOAP-ENV:Body>\n" +
                "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" +
                "           <symbol>DIS</symbol>\n" +
                "       </m:GetLastTradePrice>\n" +
                "   </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";

        StringBuilder mediator=new StringBuilder("<velocityTemplate media-type=\"xml" +
                "\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "      <person xmlns=\"\">\n" +
                "         <name>$code</name>\n" +
                "      </person>\n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"code\" expression=\"$ctx:code\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"header\"/>\n" +
                "</velocityTemplate>");

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        messageContext.setProperty("code","testcode");
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator.toString()), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator templateMediator=(VelocityTemplateMediator)mediator1;
        templateMediator.init(null);
        boolean mediate = mediator1.mediate(messageContext);
        assertTrue(mediate);//mediation is successfull
        OMElement body1 =messageContext.getEnvelope();
        String result = body1.toString();
//        System.out.println("=========================================");
//        System.out.println(body1.toString());
//        System.out.println("=========================================");
        assertXMLEqual(result,expected);
    }

    public void testArrayXMLSourceBody_targetBody() throws Exception {
        String body="\n" +
                "<people xmlns=\"\">\n" +
                "    <person>\n" +
                "      <name>asanka</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>nuwan</name>\n" +
                "      <age>28</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Eranda</name>\n" +
                "      <age>30</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Malith</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "</people>";

        String mediator="<velocityTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "    <students xmlns=\"\">\n" +
                "        #foreach($student in $students)\n" +
                "            <student>\n" +
                "                <name>$student</name>\n" +
                "            </student>\n" +
                "        #end\n" +
                "    </students>\n" +
                "      \n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"students\" expression=\"//name/text()\" type=\"string\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"body\"/>\n" +
                "</velocityTemplate>";

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator mediator2=(VelocityTemplateMediator)mediator1;
        mediator2.init(null);
        boolean mediate = mediator1.mediate(messageContext);
        assertTrue(mediate);//mediation is successfull
        String expected="<students><student><name>asanka</name></student><student><name>nuwan</name></student><student>" +
                "<name>Eranda</name></student><student><name>Malith</name></student></students>";
        OMElement body1 = messageContext.getEnvelope().getBody().getFirstElement();
        String result = body1.toString();
        assertXMLEqual(expected,result);
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
    }

    public void testArrayXML2SourceBody_targetBody() throws Exception {
        String body="\n" +
                "<people xmlns=\"\">\n" +
                "    <person>\n" +
                "      <name>asanka</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>nuwan</name>\n" +
                "      <age>28</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Eranda</name>\n" +
                "      <age>30</age>   \n" +
                "    </person>  \n" +
                "      <person>\n" +
                "      <name>Malith</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "</people>";

        String mediator="<velocityTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "    <students xmlns=\"\">\n" +
                "        #foreach($student in $students)\n" +
                "                $student\n" +
                "        #end\n" +
                "    </students>\n" +
                "      \n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"students\" expression=\"//person\" type=\"string\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"body\"/>\n" +
                "</velocityTemplate>";

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator mediator2=(VelocityTemplateMediator)mediator1;
        mediator2.init(null);
        boolean mediate = mediator1.mediate(messageContext);

        assertTrue(mediate);//mediation is successfull
        String expected="<students><person><name>asanka</name><age>27</age></person><person><name>nuwan</name>" +
                "<age>28</age></person><person><name>Eranda</name><age>30</age></person><person><name>Malith</name>" +
                "<age>27</age></person></students>";
        OMElement body1 = messageContext.getEnvelope().getBody().getFirstElement();
        String result = body1.toString();
        assertXMLEqual(expected,result);
//        System.out.println("=========================================");
//        System.out.println(result);
//        System.out.println("=========================================");
    }



}
