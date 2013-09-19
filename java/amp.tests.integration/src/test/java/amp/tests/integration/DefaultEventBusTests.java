package amp.tests.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import static org.junit.Assert.*;
import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;

public class DefaultEventBusTests {
    
	protected static FileSystemXmlApplicationContext context;
	protected static IEventBus bus;
	
	public static String[] getConfigFiles(){
		return new String[]{
				Config.Bus.All, 
				Config.Authorization.Basic, 
				Config.Topology.Simple};
	}
	
	@BeforeClass
	public static void BeforeAllTests(){
		context = new FileSystemXmlApplicationContext(getConfigFiles());
		bus = (IEventBus) context.getBean("eventBus");
	}
	
	@AfterClass
	public static void AfterAllTests(){
		bus.dispose();
		context.close();
	}
	
    @Test
    public void Should_be_able_to_publish_and_subscribe() throws Exception
    {
    	TestHandler handler = new TestHandler();
        bus.subscribe(handler);

        TestEvent sentEvent = new TestEvent();

        bus.publish(sentEvent);

        waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).getReceivedEvent(), notNullValue());
        assertEquals("Received and Sent events were not the same.", 
        		sentEvent.Id, handler.getReceivedEvent().Id);
    }

    private class TestHandler implements IEventHandler<TestEvent>{
    	
    	private TestEvent receivedEvent;
    	
    	public TestEvent getReceivedEvent(){
    		return receivedEvent;
    	}
    	
		public Class<TestEvent> getEventType() {
			return TestEvent.class;
		}

		public Object handle(TestEvent event, Map<String, String> headers) {
			receivedEvent = event;
			return null;
		}

		public Object handleFailed(Envelope envelope, Exception e) {
			e.printStackTrace();
			return null;
		}       
	
    }
}
