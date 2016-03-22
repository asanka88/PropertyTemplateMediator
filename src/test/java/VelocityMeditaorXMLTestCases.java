import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.AXIOMUtils;
import org.asanka.dev.VelocityTemplateMediator;
import org.asanka.dev.VelocityTemplateMediatorFactory;
import org.asanka.dev.testUtils.TestUtils;
import org.junit.Test;

/**
 * Created by asanka on 3/21/16.
 */
public class VelocityMeditaorXMLTestCases extends TestCase{

    public void testOneToOneXMLSourceBody_targetBody() throws Exception {
        String body="<people>\n" +
                "    <person>\n" +
                "      <name>asanka</name>\n" +
                "      <age>27</age>   \n" +
                "    </person>  \n" +
                "</people>";

        String mediator="<velocityTemplate media-type=\"xml\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "   <format>\n" +
                "      <person xmlns=\"\">\n" +
                "         <name>$name</name>\n" +
                "         <age>$age</age>\n" +
                "         <name>$code</name>\n" +
                "      </person>\n" +
                "   </format>\n" +
                "   <args>\n" +
                "      <arg name=\"name\" expression=\"//name\" type=\"string\"/>\n" +
                "      <arg name=\"age\" expression=\"//age\" type=\"string\"/>\n" +
                "      <arg name=\"code\" expression=\"$ctx:code\"/>\n" +
                "   </args>\n" +
                "   <target target-type=\"body\"/>\n" +
                "</velocityTemplate>";

        MessageContext messageContext= TestUtils.createLightweightSynapseMessageContext(body);
        messageContext.setProperty("code","testcode");
        VelocityTemplateMediatorFactory factory=new VelocityTemplateMediatorFactory();
        Mediator mediator1 = factory.createMediator(AXIOMUtil.stringToOM(mediator), null);
        if(!(mediator1 instanceof VelocityTemplateMediator)){
            fail();
        }
        VelocityTemplateMediator mediator2=(VelocityTemplateMediator)mediator1;
        mediator2.init(null);
        boolean mediate = mediator1.mediate(messageContext);

        assertTrue(mediate);//mediation is successfull
        OMElement body1 = messageContext.getEnvelope().getBody().getFirstElement();
        System.out.println("=========================================");
        System.out.println(body1.toString());
        System.out.println("=========================================");


    }


}
