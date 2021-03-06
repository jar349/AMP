﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using cmf.eventing.patterns.streaming;
using Common.Logging;
using NUnit.Framework;

namespace amp.tests.integration.Eventing.Streaming
{
    public class DefaultStreamingBusTests : DefaultEventBusTests
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(DefaultEventBusTests));
        private IStreamingEventBus _streamingBus;

        [TestFixtureSetUp]
        public override void TestFixtureSetUp()
        {
            base.TestFixtureSetUp();
            _bus = _streamingBus = _context.GetObject("IStreamingEventBus") as IStreamingEventBus;
        }

        [Test]
        [Ignore]
        public void Should_receive_all_segments_published_as_a_chunked_sequence()
        {
            ManualResetEvent signal = new ManualResetEvent(false);
            CollectionHandler handler = new CollectionHandler(signal);
            _streamingBus.SubscribeToCollection(handler);


            IList<TestStreamEvent> eventSequence = new List<TestStreamEvent>();
            eventSequence.Add(new TestStreamEvent(1));
            eventSequence.Add(new TestStreamEvent(2));
            eventSequence.Add(new TestStreamEvent(3));

            _streamingBus.PublishChunkedSequence(eventSequence);

            signal.WaitOne(TimeSpan.FromSeconds(10));

            Assert.That(handler.ReceivedEvents, Is.Not.Null, "The collection of events was not receieved before the timeout expired.");
            Assert.That(handler.ReceivedEvents.Length, Is.EqualTo(3));
            Assert.That(handler.ReceivedEvents.Select(e => e.Event), Is.Ordered.By("Sequence"));

        }

        [Test]
        public void Should_receive_all_events_published_as_a_stream()
        {
            ManualResetEvent signal = new ManualResetEvent(false);
            ReaderHandler handler = new ReaderHandler(signal);

            _streamingBus.SubscribeToReader(handler); 
            
            IList<TestStreamEvent> streamEvents = new List<TestStreamEvent>();
            streamEvents.Add(new TestStreamEvent(1));
            streamEvents.Add(new TestStreamEvent(2));
            streamEvents.Add(new TestStreamEvent(3));

            using (IEventStream stream = _streamingBus.CreateStream(typeof (TestStreamEvent).FullName))
            {
                stream.BatchLimit = 2;

                foreach (TestStreamEvent @event in streamEvents)
                {
                    stream.Publish(@event);
                }
            }

            signal.WaitOne(TimeSpan.FromSeconds(10));

            Assert.That(handler.ReceivedEvents, Is.Not.Null, "The collection of events was not receieved before the timeout expired.");
            Assert.That(handler.ReceivedEvents.Length, Is.EqualTo(3));
            Assert.That(handler.ReceivedEvents.Select(e => e.Event), Is.Ordered.By("Sequence"));
        }

        public class TestStreamEvent : TestEvent
        {
            public int Sequence { get; set; }

            public TestStreamEvent(int sequence)
            {
                Sequence = sequence;
            }

            public override string ToString()
            {
                return string.Format("Stream Event #{0}-{1}", Sequence, Id);
            }
        }

        public class CollectionHandler : IStreamingCollectionHandler<TestStreamEvent>
        {
            private readonly ManualResetEvent _signal;
            public StreamingEventItem<TestStreamEvent>[] ReceivedEvents { get; private set; }

            public CollectionHandler(ManualResetEvent signal)
            {
                _signal = signal;
            }

            public void HandleCollection(IEnumerable<StreamingEventItem<TestStreamEvent>> events)
            {
                ReceivedEvents = events.ToArray();
                _signal.Set();

                Log.Debug(string.Format("Received a collection from AMPere of size: {0}", events.Count()));

                foreach (StreamingEventItem<TestStreamEvent> eventItem in events)
                {
                    Log.Debug(string.Format("Event Item: SequenceId => {0}, Position => {1}, Content => {2}",
                        eventItem.SequenceId, eventItem.Position, eventItem.Event));
                }

                Log.Debug("Processing complete.");
            }

            public Type EventType
            {
                get { return typeof (TestStreamEvent); }
            }

            public void OnPercentCollectionReceived(double percent)
            {
                Log.Debug(string.Format("Percent of events received: {0}%", percent));
            }
        }

        public class ReaderHandler : IStreamingReaderHandler<TestStreamEvent>
        {
            private readonly ManualResetEvent _signal;
            private readonly List<StreamingEventItem<TestStreamEvent>> _receivedEvents = new List<StreamingEventItem<TestStreamEvent>>();

            public StreamingEventItem<TestStreamEvent>[] ReceivedEvents { get { return _receivedEvents.ToArray(); } }


            public ReaderHandler(ManualResetEvent signal)
            {
                _signal = signal;
            }

            public string Topic
            {
                get { return typeof (TestStreamEvent).FullName; }
            }

            public void Dispose()
            {
            }

            public void OnCompleted()
            {
                Log.Debug("Notified that last event has been processed for the stream.");
                _signal.Set();
            }

            public void OnError(Exception error)
            {
                throw new NotImplementedException();
            }

            public void OnNext(StreamingEventItem<TestStreamEvent> eventItem)
            {
                _receivedEvents.Add(eventItem);
                Log.Debug(string.Format("Message received: (sequenceId: {0}), (position: {1}), Event Value: {2}",
                    eventItem.SequenceId, eventItem.Position, eventItem.Event));
            }

            public Type EventType
            {
                get { return typeof (TestStreamEvent); }
            }
        }
    }
}
