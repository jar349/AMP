﻿namespace amp.examples.gui.messages
{
    public class ExampleRequest
    {
        public string Message { get; set; }


        public ExampleRequest()
        {
        }

        public ExampleRequest(string message)
        {
            this.Message = message;
        }
    }
}
