package amp.eventing;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.messaging.IContinuationCallback;
import amp.messaging.IMessageChainProcessor;
import amp.messaging.IMessageHandler;
import amp.messaging.IMessageProcessor;
import amp.messaging.MessageContext;
import amp.messaging.MessageException;
import amp.messaging.MessageReceiver;
import amp.messaging.MessageRegistration;
import cmf.bus.Envelope;
import cmf.bus.IEnvelopeReceiver;
import cmf.eventing.IEventConsumer;
import cmf.eventing.IEventFilterPredicate;
import cmf.eventing.IEventHandler;


/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/1/13
 */
public class DefaultEventConsumer extends MessageReceiver implements IEventConsumer, IMessageChainProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventConsumer.class);

        public DefaultEventConsumer(IEnvelopeReceiver envelopeReceiver) {
        super(envelopeReceiver);
    }

    public DefaultEventConsumer(IEnvelopeReceiver envelopeReceiver, List<IMessageProcessor> processorChain) {
        super(envelopeReceiver, processorChain);
    }

    @Override
    public <TEVENT> void subscribe(IEventHandler<TEVENT> handler) throws MessageException, IllegalArgumentException {
        Class<TEVENT> type = handler.getEventType();
        IEventFilterPredicate filterPredicate = new TypeEventFilterPredicate(type);
        subscribe(handler, filterPredicate);
    }
    
    @Override
	public <TEVENT> void subscribe(IEventHandler<TEVENT> handler,
			IEventFilterPredicate predicate) throws MessageException {

    	onMessageReceived(new EventMessageHandler<TEVENT>(handler));
    }
    
    private static class EventMessageHandler<TEVENT> implements IMessageHandler<TEVENT>{
    	private static final Logger LOG = LoggerFactory.getLogger(EventMessageHandler.class);

        private final IEventHandler<TEVENT> _handler;
  
        public EventMessageHandler(IEventHandler<TEVENT> handler){
            _handler = handler;
        }

        public Class<TEVENT> getMessageType(){
            return _handler.getEventType(); 
        }

        @Override
		public Object handle(TEVENT message, Map<String, String> headers){
            _handler.handle(message, headers);
            return null;
        }

        @Override
        public Object handleFailed(Envelope env, Exception ex){
        	LOG.warn("Failed to handle envelope.", ex);
            return null;
        }
    }
}
