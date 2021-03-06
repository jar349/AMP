package amp.eventing;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;
import amp.messaging.IMessageProcessor;
import amp.messaging.MessageException;

/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/1/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultEventBus implements IEventBus {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEventBus.class);

    protected final DefaultEventConsumer _eventConsumer;
    protected final DefaultEventProducer _eventProducer;


    public DefaultEventBus(IEnvelopeBus envelopeBus, 
    		List<IMessageProcessor> inboundProcessors,
    		List<IMessageProcessor> outboundProcessors)  {
        _eventConsumer = new DefaultEventConsumer(envelopeBus, inboundProcessors);
        _eventProducer = new DefaultEventProducer(envelopeBus, outboundProcessors);
    }


	@Override
	public void publish(Object event) throws MessageException {
		_eventProducer.publish(event);
	}

    @Override
    public void publish(Object event, Map<String, String> headers) throws MessageException {
        _eventProducer.publish(event, headers);
    }


	@Override
	public <TEVENT> void subscribe(IEventHandler<TEVENT> handler) throws MessageException {
		_eventConsumer.subscribe(handler);
	}

    @Override
    public <TEVENT> void subscribe(IEventHandler<TEVENT> handler, IEnvelopeFilterPredicate eventFilterPredicate)
            throws MessageException {
        _eventConsumer.subscribe(handler, eventFilterPredicate);
    }


	@Override
	public void dispose() {
		_eventProducer.dispose();
		_eventConsumer.dispose();
	}
}
